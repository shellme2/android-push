package com.eebbk.bfc.im.push.util;

import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.PushApplication;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID生成工具
 */
public class IDUtil {

	private static AtomicLong id = new AtomicLong(0L); 
	
	private static AtomicInteger increaseRID = new AtomicInteger(1);

	private static int ridTag = 0;

	public static final int RID_BASE = 100000;

	private static final int RID_COUNT = 10000;

	//构造函数私有，防止恶意新建
	private IDUtil(){}

	public static void init(PushApplication app) {
        ridTag = app.getPlatform().getDevice().getRidTagFromMetaData();
	}

	/**
	 * 获得一个UUID
	 */
	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		return uuid.substring(0,8)+uuid.substring(9,13)+uuid.substring(14,18)+uuid.substring(19,23)+uuid.substring(24); 
	}

	/**
	 * RID(序流水号)
	 */
	public static int getRID() {
        if (increaseRID.get() >= RID_COUNT) { //上限为一万
            increaseRID.set(1);
        }
		return ridTag * RID_BASE + increaseRID.getAndIncrement();
	}

	/**
	 * 检测app的Rid前缀，true表示为当前app的rid前缀，false表示不是当前app的rid前缀
	 */
	public static boolean checkRidTag (int rid) {
		int check = rid / RID_BASE;
		if (check == ridTag) {
			return true;
		}
		return false;
	}

	public static boolean hasRIDField(ResponseEntity responseEntity) {
		boolean has;
		int rid = responseEntity.getRID();
		if (rid == 0) {
			has = false;
		} else {
			has = true;
		}
		return has;
	}
}
