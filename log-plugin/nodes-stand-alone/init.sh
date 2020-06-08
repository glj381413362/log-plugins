#./bin/bash
# 定义颜色
BLUE_COLOR="\033[36m"
RED_COLOR="\033[31m"
GREEN_COLOR="\033[32m"
VIOLET_COLOR="\033[35m"
RES="\033[0m"

echo -e "${BLUE_COLOR}# ###################################################################################################${RES}"
echo -e "${BLUE_COLOR}#                       Docker ELK Shell Script                                                     #${RES}"
echo -e "${BLUE_COLOR}#                       Blog: http://jessica.glj-site.com:8888                                      #${RES}"
echo -e "${BLUE_COLOR}#                       CSDN: https://blog.csdn.net/qq_21239913       							  #${RES}"
echo -e "${BLUE_COLOR}#                       GitHub: https://github.com/glj381413362/common-plugin/tree/master/log-plugin#${RES}"
echo -e "${BLUE_COLOR}# ###################################################################################################${RES}"

echo -e "${GREEN_COLOR}>>>>>>>>>>>>>>>>>> The Start <<<<<<<<<<<<<<<<<<${RES}"
# 创建目录
if [ ! -d "./elasticsearch/" ]; then
echo -e "${BLUE_COLOR}---> create [elasticsearch]directory start.${RES}"
mkdir -p ./elasticsearch/master/conf ./elasticsearch/master/data ./elasticsearch/master/logs \
    ./elasticsearch/slave1/conf ./elasticsearch/slave1/data ./elasticsearch/slave1/logs \
    ./elasticsearch/slave2/conf ./elasticsearch/slave2/data ./elasticsearch/slave2/logs
fi


if [ ! -d "./kibana/" ]; then
	echo -e "${RED_COLOR}---> create [kibana]directory start.${RES}"
	mkdir -p ./kibana/conf ./kibana/logs
fi

if [ ! -d "./logstash/" ]; then
	echo -e "${GREEN_COLOR}---> create [logstash]directory start.${RES}"
 	mkdir -p ./logstash/conf ./logstash/logs
fi

if [ ! -d "./filebeat/" ]; then
	echo -e "${GREEN_COLOR}---> create [filebeat]directory start.${RES}"
	mkdir -p ./filebeat/conf ./filebeat/logs ./filebeat/data
fi

# 目录授权(data/logs 都要授读/写权限)
if [ -d "./elasticsearch/" ]; then
	echo -e "${BLUE_COLOR}---> directory authorize start.${RES}"
chmod 777 ./elasticsearch/master/data/ ./elasticsearch/master/logs/ \
    ./elasticsearch/slave1/data/ ./elasticsearch/slave1/logs/ \
    ./elasticsearch/slave2/data/ ./elasticsearch/slave2/logs
fi

if [ -d "./filebeat/" ]; then
	chmod 777 ./filebeat/data/ ./filebeat/logs/
	echo -e "${BLUE_COLOR}===> directory authorize success.${RES}"
fi

# 移动配置文件
if [ -f "./es-master.yml" ] && [ -f "./es-slave1.yml" ] && [ -f "./es-slave2.yml" ]; then
	echo -e "${BLUE_COLOR}---> move [elasticsearch]config file start.${RES}"
	mv ./es-master.yml ./elasticsearch/master/conf
	mv ./es-slave1.yml ./elasticsearch/slave1/conf
	mv ./es-slave2.yml ./elasticsearch/slave2/conf
fi

if [ -f "./kibana.yml" ]; then
	echo -e "${RED_COLOR}---> move [kibana]config file start.${RES}"
	mv ./kibana.yml ./kibana/conf
fi

if [ -f "./logstash-filebeat.conf" ]; then
 	echo -e "${GREEN_COLOR}---> move [logstash]config file start.${RES}"
 	mv ./logstash-filebeat.conf ./logstash/conf
fi


if [ -f "./filebeat.yml" ]; then
	echo -e "${GREEN_COLOR}---> move [filebeat]config file start.${RES}"
	mv ./filebeat.yml ./filebeat/conf
fi

echo -e "${GREEN_COLOR}>>>>>>>>>>>>>>>>>> The End <<<<<<<<<<<<<<<<<<${RES}"

# 部署项目
echo -e "${BLUE_COLOR}==================> Docker deploy Start <==================${RES}"
docker-compose -f docker-compose-elk-alone up --build -d
