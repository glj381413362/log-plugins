INSERT INTO mall_order (order_id,order_code,user_id,status,desc) VALUES ( 1,'MO001',1,'COMPLETE','完整订单');
INSERT INTO mall_order (order_id,order_code,user_id,status,desc) VALUES ( 2,'MO002',1,'COMPLETE','完整订单');
INSERT INTO mall_order (order_id,order_code,user_id,status,desc) VALUES ( 5,'MO005',1,'CANCEL','完整订单');
INSERT INTO mall_order (order_id,order_code,user_id,status,desc) VALUES ( 3,'MO003',2,'COMPLETE','缺少订单行');
INSERT INTO mall_order (order_id,order_code,user_id,status,desc) VALUES ( 4,'MO004',3,'COMPLETE','对应订单行缺少sku');


-- 订单MO001 对应的订单行
INSERT INTO mall_order_entry (order_entry_id,order_id,sku_id,quantity) VALUES (1,1,1,2);
INSERT INTO mall_order_entry (order_entry_id,order_id,sku_id,quantity) VALUES (2,1,2,2);

-- 订单MO004 对应订单行 缺少sku
INSERT INTO mall_order_entry (order_entry_id,order_id,sku_id,quantity) VALUES (3,4,1000,2);
-- 订单MO005 取消的行
INSERT INTO mall_order_entry (order_entry_id,order_id,sku_id,quantity) VALUES (4,5,3,2);



INSERT INTO mall_sku (sku_id,sku_code,sku_name,price) VALUES (1,'sku001','华为mate30',5000);
INSERT INTO mall_sku (sku_id,sku_code,sku_name,price) VALUES (2,'sku002','华为mate30 5G',6000);
INSERT INTO mall_sku (sku_id,sku_code,sku_name,price) VALUES (3,'sku003','华为mate30 Pro',9000);



INSERT INTO mall_user (user_id,user_code,user_name) VALUES (1,'user001','孙悟空');
INSERT INTO mall_user (user_id,user_code,user_name) VALUES (2,'user002','白骨精');
INSERT INTO mall_user (user_id,user_code,user_name) VALUES (3,'user003','猪八戒');