# OMOP numeric `concept_cd` query optimization

This option speeds up SQL Server ACT/i2b2-on-OMOP concept queries by allowing
OMOP-backed fact views to expose numeric OMOP concept ids as `int` or `bigint`
instead of `varchar`. It assumes i2b2-on-OMOP is already installed and that the
ACT-OMOP ontology tables and CRC OMOP views are already in use.

The optimization is project-scoped and conservative. It is only enabled when
the project has `CRC_ENABLE_NUMERIC_CONCEPT_CD=ON`, the CRC datasource is SQL
Server, and the target fact/view column `concept_cd` is detected as `int` or
`bigint`. Otherwise the query processor keeps the normal i2b2 string behavior.

## Why this helps

The ACT-OMOP ontology stores OMOP concept ids in metadata columns such as
`concept_dimension.concept_cd`, which are varchar in the i2b2 metadata model.
The ACT-OMOP CRC views traditionally also cast OMOP ids to varchar so they look
exactly like i2b2 fact tables.

On large OMOP tables this can force SQL Server into slow plans for concept
panels. With this option, a numeric fact view can be queried as:

```sql
f.concept_cd IN (
  select TRY_CONVERT(int, concept_cd) AS concept_cd
  from concept_dimension
  where concept_path LIKE ...
)
```

That lets SQL Server compare the fact-side OMOP concept id as a number and use
the native OMOP indexes more effectively.

## 1. Use numeric `concept_cd` in the OMOP fact views

Update only the OMOP-backed fact views where `concept_cd` represents an OMOP
concept id. Do not change standard i2b2 `observation_fact.concept_cd`, and do
not change fact views that intentionally use alphanumeric codes.

For example, in the SQL Server ACT-OMOP view script:

```text
i2b2-data/edu.harvard.i2b2.data/Release_1-8/NewInstall/Crcdata/act-omop/scripts/sqlserver/BUILD_ACT_OMOP_VIEWS_ALL_MSSQL.sql
```

change views like this:

```sql
-- Before
cast(condition_concept_id as varchar(50)) AS CONCEPT_CD

-- After
condition_concept_id AS CONCEPT_CD
```

Apply the same pattern to the OMOP concept-id columns used by other fact views,
for example:

```sql
drug_concept_id AS CONCEPT_CD
procedure_concept_id AS CONCEPT_CD
measurement_concept_id AS CONCEPT_CD
observation_concept_id AS CONCEPT_CD
```

For non-standard/source concept views, use the source concept id column if the
ontology points to those views:

```sql
condition_source_concept_id AS CONCEPT_CD
drug_source_concept_id AS CONCEPT_CD
procedure_source_concept_id AS CONCEPT_CD
measurement_source_concept_id AS CONCEPT_CD
observation_source_concept_id AS CONCEPT_CD
```

Leave any view whose `concept_cd` is built from text or concatenated values as
varchar. For example, `COVID_LAB_VIEW` currently builds `concept_cd` with
`CONCAT(...)`; that is not a numeric `concept_cd` candidate.

After changing a view, confirm SQL Server sees the column as `int` or `bigint`:

```sql
select
  OBJECT_NAME(object_id) as object_name,
  name as column_name,
  TYPE_NAME(system_type_id) as data_type
from sys.columns
where object_id = OBJECT_ID('dbo.CONDITION_VIEW')
  and name = 'CONCEPT_CD';
```

## 2. Confirm ontology fact-table columns are view-qualified

The query processor detects the target fact/view column from ontology metadata.
For this optimization, the concept metadata should identify the OMOP fact view
in `C_FACTTABLECOLUMN`, for example:

```text
CONDITION_VIEW.CONCEPT_CD
DRUG_VIEW.CONCEPT_CD
PROCEDURE_VIEW.CONCEPT_CD
MEASUREMENT_VIEW.CONCEPT_CD
OBSERVATION_VIEW.CONCEPT_CD
```

This allows one project to mix numeric OMOP fact views and ordinary
alphanumeric i2b2 fact tables. If a concept only says `concept_cd`, the
processor cannot safely determine which physical fact/view column to inspect
and will keep the legacy SQL.

## 3. Add the project parameter

Enable the optimization for the project in `PM_PROJECT_PARAMS`.

```sql
insert into PM_PROJECT_PARAMS
  (DATATYPE_CD, PROJECT_ID, PARAM_NAME_CD, VALUE, CHANGEBY_CHAR, STATUS_CD)
values
  ('T', 'ACT', 'CRC_ENABLE_NUMERIC_CONCEPT_CD', 'ON', 'i2b2', 'A');
```

Use your actual project id instead of `ACT`.

If a row already exists, update it:

```sql
update PM_PROJECT_PARAMS
set VALUE = 'ON',
    STATUS_CD = 'A'
where PROJECT_ID = 'ACT'
  and PARAM_NAME_CD = 'CRC_ENABLE_NUMERIC_CONCEPT_CD';
```

To disable the optimization without changing views, set the value to anything
other than `ON` or remove/inactivate the parameter.

## 4. Refresh runtime state

The CRC caches project parameters when users enter a project. After changing
the PM parameter, have users log out and back in, or restart the relevant i2b2
services.

If a query definition already has generated SQL stored in `QT_QUERY_MASTER`,
rerun or recreate the query so the query processor can generate SQL with the
new numeric conversion. Existing stored generated SQL may still contain the old
varchar comparison.

## 5. Verify the generated SQL

The CRC logs INFO messages when it detects and uses a numeric `concept_cd`
fact column:

```text
Detected numeric fact column CONDITION_VIEW.CONCEPT_CD with type int
Using TRY_CONVERT(int) for concept_dimension.CONCEPT_CD because CONDITION_VIEW.CONCEPT_CD is numeric
```

The generated SQL should contain a concept_dimension subquery like:

```sql
CONDITION_VIEW.CONCEPT_CD IN (
  select TRY_CONVERT(int, CONCEPT_CD) AS CONCEPT_CD
  from dbo.concept_dimension
  where concept_path LIKE ...
)
```

If the generated SQL still says `select CONCEPT_CD from concept_dimension`, the
usual causes are:

- `CRC_ENABLE_NUMERIC_CONCEPT_CD` is not `ON` for the project.
- The user session still has old project parameters cached.
- The ontology fact-table column is not view-qualified.
- SQL Server reports the target view column as `varchar`, not `int` or
  `bigint`.

## Scope and cautions

This optimization is for SQL Server setfinder concept queries using the default
1.7 query builder. It is intended for OMOP-backed fact views where `concept_cd`
is an OMOP concept id.

Do not enable it by changing standard i2b2 fact tables to numeric
`concept_cd`; ordinary i2b2 projects commonly rely on alphanumeric concept
codes. The project parameter and per-view type detection are there so existing
i2b2 projects and mixed projects continue to work safely.
