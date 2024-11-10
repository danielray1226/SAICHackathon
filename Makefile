#Why not maven?
#Most of this is compiling/packaging javascript and restarting tomcat/vite/chromium, maven is for compiling java and tracking dependancies for java libraries.
#Makefile was more suitable as a result for adhoc scripts. (custom scripts/commands).

where-am-i = $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST)) 
THIS_MAKEFILE := $(call where-am-i)
#$(info $(THIS_MAKEFILE))
#method to find location of the current makefile
ROOT := $(dir $(THIS_MAKEFILE))
#directory of current makefil
JAVAC?=javac 
SRC_JAVA:=$(ROOT)/src
JVM_VERSION:=11
JAVA_BUILD:=$(ROOT)/webapp/WEB-INF/classes
#where to put class files
ALL_LIBS_LIST:=$(shell find $(ROOT)/webapp/WEB-INF/lib $(ROOT)/SERVER -name '*.jar' -printf '%p:')
#list of all .jar for tomcat library and web-inf libraries
#$(info "All Libs List " $(ALL_LIBS_LIST))

all: war

local_ux: 
	@cd ${ROOT}/UX/react-api-tester && npm install
	@echo "stopping old vite"
	@bash -c 'pid=`fuser 5173/tcp 2>/dev/null | tr -d '[:blank:]'` && test -z "$$pid" || { echo "killing $$pid"; kill -SIGKILL $$pid; }'
	@echo "starting new vite"
	@cd ${ROOT}/UX/react-api-tester && bash -c "npm run dev &"
	@echo "starting insecure chromium"
	@${ROOT}/start_chromium.sh "http://localhost:5173/"
ux:
	@cd ${ROOT}/UX/react-api-tester && npm install
	@cd ${ROOT}/UX/react-api-tester && npm run build
	@rm -f ${ROOT}/webapp/index.html 
	@rm -rf ${ROOT}/webapp/assets 
	@cp ${ROOT}/UX/react-api-tester/dist/index.html ${ROOT}/webapp/index.html 
	@cp -a ${ROOT}/UX/react-api-tester/dist/assets ${ROOT}/webapp/assets  
	

java-compile: 
	@echo Compile java
	@rm -rf $(JAVA_BUILD)
	@mkdir -p $(JAVA_BUILD)
	@rm -f $(JAVA_BUILD)/java_list
	@echo JAVAC VERSION
	$(JAVAC) -version
	@find $(SRC_JAVA) -name '*.java' > $(JAVA_BUILD)/java_list
	@$(JAVAC) -encoding utf8 -source $(JVM_VERSION) -target $(JVM_VERSION) \
		-cp $(ALL_LIBS_LIST) \
		-d $(JAVA_BUILD) @$(JAVA_BUILD)/java_list
	@rm -f $(JAVA_BUILD)/java_list

war:  java-compile ux
	rm -rf $(ROOT)/target
	mkdir -p $(ROOT)/target
	rm -rf $(ROOT)/local_tomcat/webapps
	mkdir -p $(ROOT)/local_tomcat/webapps
	cd $(ROOT)/webapp && zip -rq $(ROOT)/target/ROOT.war .
	cp $(ROOT)/target/ROOT.war $(ROOT)/local_tomcat/webapps/

local_tomcat: war uc
	$(ROOT)/start_local_tomcat.sh
	
uc:
	@bash -c 'pid=`fuser 9099/tcp 2>/dev/null | tr -d '[:blank:]'` && test -z "$$pid" || { echo "killing $$pid"; kill -SIGKILL $$pid; }'
	java -jar $(ROOT)/uc/uc-lib-server.jar >$(ROOT)/uc/uc.log 2>&1 &

.PHONY: all uc ux java-compile war local_tomcat #Makefile assumes all targets are files, .PHONY ensures that it will be re-executed