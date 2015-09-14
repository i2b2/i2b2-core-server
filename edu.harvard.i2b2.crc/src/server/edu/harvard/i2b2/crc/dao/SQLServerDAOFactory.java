package edu.harvard.i2b2.crc.dao;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.crc.dao.pdo.IMetadataDao;
import edu.harvard.i2b2.crc.dao.pdo.IPageDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryEidDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryModifierDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryPidDao;
import edu.harvard.i2b2.crc.dao.pdo.MetadataDao;
import edu.harvard.i2b2.crc.dao.pdo.ObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.PageTotalDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryEidDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryModifierDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryPidDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryModifierDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.role.IPriviledgeDao;
import edu.harvard.i2b2.crc.dao.role.PriviledgeSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.AnalysisPluginSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.EncounterSetCollectionSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.IAnalysisPluginDao;
import edu.harvard.i2b2.crc.dao.setfinder.IEncounterSetCollectionDao;
import edu.harvard.i2b2.crc.dao.setfinder.IPatientSetCollectionDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryBreakdownTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryPdoMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryRequestDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryStatusTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.crc.dao.setfinder.PatientSetCollectionSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryBreakdownTypeSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryInstanceSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryMasterSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryPdoMasterSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryRequestSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryResultInstanceSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryResultTypeSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.QueryStatusTypeSpringDao;
import edu.harvard.i2b2.crc.dao.setfinder.XmlResultSpringDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class SQLServerDAOFactory implements IDAOFactory {

	private DataSourceLookup dataSourceLookup = null,
			originalDataSourceLookup = null;
	private DataSource dataSource = null;
	private QueryProcessorUtil crcUtil = null;
	/** log **/
	protected final static Log log = LogFactory
			.getLog(SQLServerDAOFactory.class);

	public SQLServerDAOFactory(DataSourceLookup dataSourceLookup,
			DataSourceLookup originalDataSourceLookup) throws I2B2DAOException {

		this.dataSourceLookup = dataSourceLookup;
		this.originalDataSourceLookup = originalDataSourceLookup;
		crcUtil = QueryProcessorUtil.getInstance();
		String dataSourceName = dataSourceLookup.getDataSource();
		log.info("Using datasource " + dataSourceName);
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			dataSource = ServiceLocator.getInstance().getAppServerDataSource(
					dataSourceName);
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting application/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}

	}

	//
	public class SQLPatientDataDAOFactory implements PatientDataDAOFactory {
		private DataSourceLookup dataSourceLookup = null,
				orignalDataSourceLookup = null;
		private DataSource dataSource = null;

		public SQLPatientDataDAOFactory(DataSource dataSource,
				DataSourceLookup dataSourceLookup,
				DataSourceLookup orignalDataSourceLookup) {
			this.dataSourceLookup = dataSourceLookup;
			this.dataSource = dataSource;
			this.orignalDataSourceLookup = dataSourceLookup;

		}

		public ObservationFactDao getObservationFactDAO() {
			// TODO Auto-generated method stub
			//
			return new ObservationFactDao(dataSourceLookup, dataSource);
			// return null;

		}

		public PdoQueryConceptDao getPdoQueryConceptDAO() {
			// TODO Auto-generated method stub
			return new PdoQueryConceptDao(dataSourceLookup, dataSource);
			// return null;
		}
		
		public PdoQueryModifierDao getPdoQueryModifierDAO() {
			// TODO Auto-generated method stub
			return new PdoQueryModifierDao(dataSourceLookup, dataSource);
			// return null;
		}

		public PdoQueryPatientDao getPdoQueryPatientDAO() {
			// TODO Auto-generated method stub
			return new PdoQueryPatientDao(dataSourceLookup, dataSource);
			// return null;
		}

		public PdoQueryProviderDao getPdoQueryProviderDAO() {
			// TODO Auto-generated method stub
			return new PdoQueryProviderDao(dataSourceLookup, dataSource);
		}

		public PdoQueryVisitDao getPdoQueryVisitDAO() {
			// TODO Auto-generated method stub
			return new PdoQueryVisitDao(dataSourceLookup, dataSource);
		}

		public TablePdoQueryConceptDao getTablePdoQueryConceptDAO() {
			// TODO Auto-generated method stub
			return new TablePdoQueryConceptDao(dataSourceLookup, dataSource);
		}
		
		public TablePdoQueryModifierDao getTablePdoQueryModifierDAO() {
			// TODO Auto-generated method stub
			return new TablePdoQueryModifierDao(dataSourceLookup, dataSource);
		}

		public TablePdoQueryPatientDao getTablePdoQueryPatientDAO() {
			// TODO Auto-generated method stub
			return new TablePdoQueryPatientDao(dataSourceLookup, dataSource);
		}

		public TablePdoQueryProviderDao getTablePdoQueryProviderDAO() {
			// TODO Auto-generated method stub
			return new TablePdoQueryProviderDao(dataSourceLookup, dataSource);
		}

		public TablePdoQueryVisitDao getTablePdoQueryVisitDAO() {
			// TODO Auto-generated method stub
			return new TablePdoQueryVisitDao(dataSourceLookup, dataSource);
		}

		public DataSourceLookup getDataSourceLookup() {
			return dataSourceLookup;
		}

		public DataSourceLookup getOriginalDataSourceLookup() {
			return originalDataSourceLookup;
		}
		
		public DataSource getDataSource() {
			return dataSource;
		}
		
		public IPageDao getPageDAO() {
			// TODO Auto-generated method stub
			return new PageTotalDao(dataSourceLookup, dataSource);
		}

		public IPdoQueryPidDao getPdoQueryPidDAO() {
			return new PdoQueryPidDao(dataSourceLookup, dataSource);
		}

		public IPdoQueryEidDao getPdoQueryEidDAO() {
			return new PdoQueryEidDao(dataSourceLookup, dataSource);
		}

		public IMetadataDao getMetadataDAO() {
			return new MetadataDao(dataSourceLookup, dataSource);
		}

	}

	public class SQLSetFinderDAOFactory implements SetFinderDAOFactory {
		private DataSourceLookup dataSourceLookup = null,
				orignalDataSourceLookup = null;
		private DataSource dataSource = null;

		public SQLSetFinderDAOFactory(DataSource dataSource,
				DataSourceLookup dataSourceLookup,
				DataSourceLookup orignalDataSourceLookup) {
			this.dataSourceLookup = dataSourceLookup;
			this.dataSource = dataSource;
			this.orignalDataSourceLookup = dataSourceLookup;

		}

		public IXmlResultDao getXmlResultDao() {
			// TODO Auto-generated method stub
			return new XmlResultSpringDao(dataSource, dataSourceLookup);
		}

		public IPatientSetCollectionDao getPatientSetCollectionDAO() {
			// TODO Auto-generated method stub
			return new PatientSetCollectionSpringDao(dataSource,
					dataSourceLookup);
		}

		public IEncounterSetCollectionDao getEncounterSetCollectionDAO() {
			// TODO Auto-generated method stub
			return new EncounterSetCollectionSpringDao(dataSource,
					dataSourceLookup);
		}

		public IQueryResultInstanceDao getPatientSetResultDAO() {
			// TODO Auto-generated method stub
			return new QueryResultInstanceSpringDao(dataSource,
					dataSourceLookup);
		}

		public IQueryInstanceDao getQueryInstanceDAO() {
			// TODO Auto-generated method stub
			return new QueryInstanceSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryMasterDao getQueryMasterDAO() {
			// TODO Auto-generated method stub
			return new QueryMasterSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryPdoMasterDao getQueryPdoMasterDAO() {
			// TODO Auto-generated method stub
			return new QueryPdoMasterSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryRequestDao getQueryRequestDAO() {
			// TODO Auto-generated method stub
			return new QueryRequestSpringDao(dataSource, dataSourceLookup);
		}

		public DataSourceLookup getDataSourceLookup() {
			return dataSourceLookup;
		}

		public DataSourceLookup getOriginalDataSourceLookup() {
			return originalDataSourceLookup;
		}
		
		public DataSource getDataSource() {
			return dataSource;
		}
		

		public IQueryResultTypeDao getQueryResultTypeDao() {

			return new QueryResultTypeSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryStatusTypeDao getQueryStatusTypeDao() {

			return new QueryStatusTypeSpringDao(dataSource, dataSourceLookup);
		}

		public IAnalysisPluginDao getAnalysisPluginDao() {
			return new AnalysisPluginSpringDao(dataSource, dataSourceLookup);
		}

		public IPriviledgeDao getPriviledgeDao() {
			return new PriviledgeSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryBreakdownTypeDao getQueryBreakdownTypeDao() {
			return new QueryBreakdownTypeSpringDao(dataSource, dataSourceLookup);
		}

	}

	public PatientDataDAOFactory getPatientDataDAOFactory() {
		// TODO Auto-generated method stub
		return new SQLPatientDataDAOFactory(dataSource, dataSourceLookup,
				originalDataSourceLookup);
	}

	public SetFinderDAOFactory getSetFinderDAOFactory() {
		// TODO Auto-generated method stub
		return new SQLSetFinderDAOFactory(dataSource, dataSourceLookup,
				originalDataSourceLookup);
	}

}
