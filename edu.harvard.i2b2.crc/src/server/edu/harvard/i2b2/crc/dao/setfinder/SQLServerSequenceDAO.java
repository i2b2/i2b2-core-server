package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class SQLServerSequenceDAO extends CRCDAO {
	/** logger **/
	protected final Log log = LogFactory.getLog(SQLServerSequenceDAO.class);
	
	public static final String QT_QUERY_MASTER_ID = "QT_QUERY_MASTER_ID";
    public static final String QT_QUERY_INSTANCE_ID = "QT_QUERY_INSTANCE_ID";
    public static final String QT_QUERY_RESULT_INSTANCE_ID="QT_QUERY_RESULT_INSTANCE_ID";
    public static final String QT_PATIENT_SET_COLLECTION_ID = "QT_PATIENT_SET_COLLECTION_ID";
    public static final String QT_XML_RESULT_ID = "QT_XML_RESULT_ID"; 
    public static final String QT_PATIENT_ENC_COLLECIOTN_ID = "QT_PATIENT_ENC_COLLECIOTN_ID";
   
    private DataSourceLookup dataSourceLookup = null;
    
    public SQLServerSequenceDAO(DataSource dataSource,DataSourceLookup dataSourceLookup) { 
    	setDataSource(dataSource);
    	this.dataSourceLookup = dataSourceLookup;
    }
    
	public int generateSequence(String sequenceName) throws I2B2DAOException {
		int newSeq = 0;
		Connection conn = null;
		try { 
			conn = dataSource.getConnection();
			CallableStatement cstmt = conn.prepareCall("{? = call dbo.p_get_next_sequence(?)}");
	    	cstmt.registerOutParameter(1, Types.INTEGER);
	        cstmt.setString(2, sequenceName);
	        cstmt.execute();
	        newSeq = cstmt.getInt(1);
		} catch (SQLException sqlEx) { 
			log.error("SQLServerSequenceDAO:generateSequence"+sqlEx.getMessage(),sqlEx);
			throw new I2B2DAOException("SQLServerSequenceDAO:generateSequence"+sqlEx.getMessage(),sqlEx);
		} finally { 
			if (conn !=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		return newSeq;
        
	}
}
