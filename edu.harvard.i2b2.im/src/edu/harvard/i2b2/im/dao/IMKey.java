package edu.harvard.i2b2.im.dao;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import org.apache.commons.codec.digest.DigestUtils;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.wdo.SetKeyType;

public class IMKey {

	private static Hashtable<String, String> imkey = new Hashtable<String, String>();

	public static String getKey(ProjectType projectInfo) {
		return imkey.get(projectInfo.getId());
	}


	public static int setKey(SetKeyType requestType, ProjectType projectInfo,
			String projectID) throws UnsupportedEncodingException, NoSuchAlgorithmException, I2B2Exception {
		if (projectInfo == null) return -1;

		if (!projectInfo.getRole().contains("ADMIN") && !projectInfo.getRole().contains("MANAGER"))
			return -11111;
		if ( (requestType.getKey() == null) || (requestType.getKey().equals("")))
		{
			imkey.remove(projectInfo.getId());
		} else {
			if (projectInfo.getKey() != null)
			{
				String key = DigestUtils.md5Hex(requestType.getKey()).substring(0, 3);
				if (!key.equals(projectInfo.getKey()))
					return -1;
			}
			imkey.put(projectInfo.getId(), requestType.getKey());
			//		IMKey.imkey = requestType.getKey();
		}
		return 0;
	}

	public static int isKeySet( ProjectType projectInfo ) throws UnsupportedEncodingException, NoSuchAlgorithmException, I2B2Exception {
		if (projectInfo == null) return -1;

		if (!projectInfo.getRole().contains("ADMIN") && !projectInfo.getRole().contains("MANAGER")
				&& !projectInfo.getRole().contains("DATA_PROT"))
			return -11111;

		if (imkey.containsKey(projectInfo.getId()))
			return 1;
		else 
			return 0;

	}


	public static int isKeySet( ProjectType projectInfo, String projectId) throws UnsupportedEncodingException, NoSuchAlgorithmException, I2B2Exception {
		if (projectInfo == null) return -1;

		if (!projectInfo.getRole().contains("ADMIN") && !projectInfo.getRole().contains("MANAGER")
				&& !projectInfo.getRole().contains("DATA_PROT"))
			return -11111;
		
		if (!projectInfo.getRole().contains("ADMIN") && !projectInfo.getId().equals(projectId))
			return -22222;

		if (imkey.containsKey(projectId))
			return 1;
		else 
			return 0;

	}



}
