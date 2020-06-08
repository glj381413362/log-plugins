package com.enhance.constant;

/**
 * <p>
 * 日志注解里面的常量
 * </p>
 *
 * @author 龚梁钧 2019/06/27 18:16
 */
public interface LogConst {
	/**
	 * 操作
	 * 为了节省日志文件大小，这些常量使用单字母代替
	 */
	Action action = Action.Q;

	enum Action {
		/**
		 * A 新增
		 */
		A,
		/**
		 * D 删除
		 */
		D,
		/**
		 * U 更新
		 */
		U,
		/**
		 * Q 查询
		 */
		Q,
		/**
		 * LQ 查询列表
		 */
		LQ,
		/**
		 * Q 查询
		 */
		NULL;
	}

	/**
	 * 对象类型
	 */
	String ITEM_TYPE_CONFIG = "config";
}
