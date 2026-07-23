# Ontology search `key_name` display paths

The Ontology cell can include a human-readable parent path with name search
results. This is exposed as the `key_name` element on each returned
`concept`. Clients request it by setting `keyname="true"` on a
`get_name_info` request.

This feature is useful when search results contain many identically named or
similarly named terms. The normal `key` field remains the canonical i2b2 path
used for later API calls and query construction; `key_name` is intended for
display.

## Where it is implemented

The request and response shape is defined in:

- `edu.harvard.i2b2.xml/xsd/cell/ont_1.1/ONT_QRY.xsd`
  - `vocab_requestType` includes the `keyname` boolean attribute.
- `edu.harvard.i2b2.xml/xsd/cell/ont_1.1/ONT_RESP.xsd`
  - `conceptType` includes the `key_name` response element.

The request is handled by:

- `edu.harvard.i2b2.ontology/src/core/edu/harvard/i2b2/ontology/ws/OntologyService.java`
  - `getNameInfo(...)`
- `edu.harvard.i2b2.ontology/src/core/edu/harvard/i2b2/ontology/delegate/GetNameInfoHandler.java`
  - unmarshals the `get_name_info` request and calls `ConceptDao.findNameInfo`.

The `key_name` value is constructed in:

- `edu.harvard.i2b2.ontology/src/core/edu/harvard/i2b2/ontology/dao/ConceptDao.java`
  - `findNameInfo(...)`, in the `vocabType.isKeyname()` branch.

The generated Java accessor is:

- `edu.harvard.i2b2.ontology/gensrc/edu/harvard/i2b2/ontology/datavo/vdo/ConceptType.java`
  - `getKeyName()` / `setKeyName(...)`

## API endpoint

Use the Ontology cell `getNameInfo` operation:

```text
POST /i2b2/services/OntologyService/getNameInfo
```

The feature is not a separate web service operation. It is an optional behavior
on `get_name_info`.

## Request

Set `keyname="true"` on the `get_name_info` element. Use `type="core"` or
`type="all"` when the client needs the full concept fields; the default return
type only asks the database for `c_name`.

Example message body:

```xml
<message_body>
  <ont:get_name_info
      xmlns:ont="http://www.i2b2.org/xsd/cell/ont/1.1/"
      category="ICD10_ICD9"
      type="core"
      max="200"
      hiddens="false"
      synonyms="false"
      keyname="true">
    <match_str strategy="contains">gout</match_str>
    <self>\\ICD10_ICD9\</self>
  </ont:get_name_info>
</message_body>
```

Important request fields:

- `category`: the table code to search, such as `ICD10_ICD9`. Use `@` to
  search visible categories.
- `match_str`: the search text. Supported strategies are `exact`, `left`,
  `right`, and `contains`.
- `type`: use `core` for the normal concept payload; use `all` when date and
  source fields are also needed.
- `max`: maximum number of concepts returned by the handler. `key_name`
  lookups are only performed up to this result count.
- `hiddens`: include hidden concepts when `true`.
- `synonyms`: include synonym rows when `true`.
- `keyname`: include the display path when `true`.

## Response

Each returned `concept` may include `key_name`:

```xml
<concept>
  <level>4</level>
  <key>\\ICD10_ICD9\ICD10\Diseases\Gout\Idiopathic gout\</key>
  <key_name>\Diagnoses\Diseases\Gout\Idiopathic gout\</key_name>
  <name>Idiopathic gout</name>
  <synonym_cd>N</synonym_cd>
  <visualattributes>LA</visualattributes>
  ...
</concept>
```

`key` is the canonical ontology key. `key_name` is a display label made from
the concept's parent rows and the result concept's `name`.

## How `key_name` is built

For each returned concept, `ConceptDao.findNameInfo(...)`:

1. Removes the table-code prefix from the result `key`.
2. Finds the result's parent path.
3. Runs a recursive parent query against the metadata table to retrieve each
   parent row's `c_name`.
4. Joins parent names with backslashes.
5. Appends the result concept's own `name`.

The generated value is slash-delimited and has leading and trailing
backslashes, for example:

```text
\Diagnoses\Diseases\Gout\Idiopathic gout\
```

When a category path has more than one path component, the implementation
omits the leading table-code component from the display path. If the category
itself does not exist as a row in the ontology table, the category name from
`table_access.c_name` is inserted manually.

Parent lookup results are cached per parent path within a single request, so
multiple matching children under the same parent reuse the same parent display
path.

## Client guidance

Use `key_name` only as display text. Continue using `key` when passing a term
to `getTermInfo`, `getChildren`, CRC query definitions, or any API that expects
an ontology path.

Treat `key_name` as optional. Older servers, requests without
`keyname="true"`, and results beyond the `max` lookup limit may not populate
it. A client should fall back to `name`, `tooltip`, or `key` when `key_name` is
empty.

Request `keyname="true"` only when the UI needs parent context. It adds parent
path lookups to search processing, although repeated parents are cached during
the request.

## Related `getTermInfo` usage

After a user selects a search result, pass the returned `key` to
`getTermInfo`. Do not pass `key_name`.

```xml
<message_body>
  <ont:get_term_info
      xmlns:ont="http://www.i2b2.org/xsd/cell/ont/1.1/"
      type="core"
      blob="true"
      hiddens="true"
      synonyms="false">
    <self>\\ICD10_ICD9\ICD10\Diseases\Gout\Idiopathic gout\</self>
  </ont:get_term_info>
</message_body>
```

`getTermInfo` returns concept details for the canonical term path. The
human-readable display path is produced by the search API's `keyname` option,
not by a separate `getTermInfo` parameter.

## Returning parent node details

Clients that need full concept blocks for search results and their parent
nodes can request them directly from `getNameInfo` by setting
`ancestors="true"`.

```xml
<message_body>
  <ont:get_name_info
      xmlns:ont="http://www.i2b2.org/xsd/cell/ont/1.1/"
      category="ICD10_ICD9"
      type="core"
      max="200"
      hiddens="false"
      synonyms="false"
      keyname="true"
      ancestors="true">
    <match_str strategy="contains">gout</match_str>
    <self>\\ICD10_ICD9\</self>
  </ont:get_name_info>
</message_body>
```

With `ancestors="true"`, the response is still the normal `concepts` payload.
It contains matched search results plus their ancestor nodes. Matched search
results include an optional marker:

```xml
<search_result>true</search_result>
```

Ancestor rows omit `search_result`; clients should treat a missing value as
false. If a concept is both a matched result and an ancestor of another result,
the matched result row wins and is returned with `search_result=true`.

Shared ancestors are returned once per searched ontology table, so clients
avoid repeatedly receiving the same parent rows when many search results have
common parents. The search hits are reduced before ancestor lookup, using the
same descendant-suppression rule as `reducedResults=true`: when a matching
active parent term is returned, matching descendant terms under that parent are
not also marked as search results. A client can associate ancestors with a
marked search result by comparing canonical `key` prefixes; for example, a
returned parent key belongs to a search result when the search result's key
starts with the parent key.

When `ancestors="true"`, `max` limits the number of marked search results.
Ancestor rows are additional context and do not count against that search-hit
limit.
