DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order` (
  `order_id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `order_code` varchar(50) NOT NULL DEFAULT '' COMMENT '订单编号',
  `user_id` int(20) NOT NULL  COMMENT '用户id',
  `status` varchar(30) NOT NULL DEFAULT '' COMMENT '订单状态',
  `desc` varchar(20) NOT NULL DEFAULT '' COMMENT '订单描述',
  PRIMARY KEY (`order_id`)
);

DROP TABLE IF EXISTS `mall_order_entry`;
CREATE TABLE `mall_order_entry` (
  `order_entry_id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单行id',
  `order_id` int(20) NOT NULL COMMENT '订单id',
  `sku_id` int(20) NOT NULL  COMMENT '商品id',
  `quantity` int(20) NOT NULL   COMMENT '数量',
  PRIMARY KEY (`order_entry_id`)
);

DROP TABLE IF EXISTS `mall_sku`;
CREATE TABLE `mall_sku` (
  `sku_id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `price` int(20) NOT NULL DEFAULT 0 COMMENT '价格',
  `sku_code` varchar(100) NOT NULL DEFAULT '' COMMENT '商品编码',
  `sku_name` varchar(100) NOT NULL DEFAULT '' COMMENT '商品名称',
  PRIMARY KEY (`sku_id`)
);

DROP TABLE IF EXISTS `mall_user`;
CREATE TABLE `mall_user` (
  `user_id` int(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(100) NOT NULL DEFAULT 0 COMMENT '用户名称',
  `user_code` varchar(100) NOT NULL DEFAULT '' COMMENT '用户编码',
  PRIMARY KEY (`user_id`)
);
