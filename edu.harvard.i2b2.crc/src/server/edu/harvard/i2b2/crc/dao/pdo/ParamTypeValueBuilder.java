package edu.harvard.i2b2.crc.dao.pdo;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;

public class ParamTypeValueBuilder {
	private  DTOFactory dtoFactory = new DTOFactory();

	public List<ParamType> buildParamType(ResultSet resultSet, MetadataDao.TableMetaData tableMetaData) { 
		return null;
	}
	
	public ParamType buildParamType(ParamType metaParamType,String fieldPrefix, String lookupFieldSuffix, ResultSet rowSet) throws SQLException { 
		ParamType paramType = new ParamType();
		
		paramType.setColumn(metaParamType.getColumn());
		paramType.setColumnDescriptor(metaParamType.getColumnDescriptor());
		
		if (lookupFieldSuffix != null && !metaParamType.getType().startsWith("defaulted:")) { 
			paramType.setName(rowSet.getString(metaParamType.getColumn() + lookupFieldSuffix  ));
		} 
		if (metaParamType.getType().startsWith("defaulted:")) { 
			paramType.setType(metaParamType.getType().substring(10));
		} else { 
			paramType.setType(metaParamType.getType());
		}
		//paramType.setValue(rowSet.getString(fieldPrefix + metaParamType.getColumn()));
		
		if (metaParamType.getType().equalsIgnoreCase("dateTime")) { 
			Date paramDate = rowSet.getTimestamp(fieldPrefix + metaParamType.getColumn());
			if (paramDate != null) { 
				paramType.setValue(dtoFactory.getXMLGregorianCalendar(paramDate.getTime()).toXMLFormat());
			}
		} else { 
			paramType.setValue(rowSet.getString(fieldPrefix + metaParamType.getColumn()));
		}
		
		return paramType;
	}
	
}
