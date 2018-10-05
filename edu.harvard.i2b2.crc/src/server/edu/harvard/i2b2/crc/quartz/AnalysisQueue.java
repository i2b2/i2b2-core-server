/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.quartz;

public class AnalysisQueue {

	public static AnalysisQueue.QueueType getNextQueue(String queueName) {
		QueueType queueType = QueueType.valueOf(queueName);
		switch (queueType) {
		case FILLER:
			return QueueType.MEDIUM_QUEUE;
		case MEDIUM_QUEUE:
			return QueueType.LARGE_QUEUE;
		case LARGE_QUEUE:
			return QueueType.NO_MORE_QUEUE;
		default:
			return QueueType.NO_MORE_QUEUE;
		}
	}

	public enum QueueType {
		FILLER, MEDIUM_QUEUE, LARGE_QUEUE, NO_MORE_QUEUE;
	}
}
