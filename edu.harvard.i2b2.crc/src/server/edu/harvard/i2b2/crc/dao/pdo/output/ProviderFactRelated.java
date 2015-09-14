/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.output;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;


/**
 * Class to generate select, join, where clause
 * for Provider dimenstion based on pdo's  OutputOptionType
 * $Id: ProviderFactRelated.java,v 1.3 2007/08/31 14:43:33 rk903 Exp $
 * @author rkuttan
 */
public class ProviderFactRelated extends FactRelated {
    public ProviderFactRelated(OutputOptionType outputOptionType) {
        super(outputOptionType);
    }

    public String getSelectClause() {
        String selectClause = "";

        if (isSelected()) {
            selectClause = " provider.provider_id provider_provider_id, provider.provider_path provider_provider_path ";

            if (isSelectDetail()) {
                selectClause += ", provider.name_char provider_name_char ";
            }

            if (isSelectBlob()) {
                selectClause += ", provider.provider_blob provider_provider_blob ";
            }

            if (isSelectStatus()) {
                selectClause += " , provider.update_date provider_update_date, provider.download_date provider_download_date, provider.import_date provider_import_date, provider.sourcesystem_cd provider_sourcesystem_cd, provider.upload_id provider_upload_id ";
            }
        }

        return selectClause;
    }

    public String joinClause() {
        if (isSelected()) {
            return " left join PROVIDER_DIMENSION   provider   on (obs.provider_id = provider.provider_id) ";
        } else {
            return "";
        }
    }

    public String getProviderIdFromResultSet(ResultSet resultSet)
        throws SQLException {
        return resultSet.getString("obs_provider_id");
    }
}
