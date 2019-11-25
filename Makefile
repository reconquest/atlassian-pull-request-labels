run@%:
	@mkdir -p targets/$*
	@ln -sTf targets/$* target
	@atlas-run -Dbitbucket.version=$* -DskipTests=true -Datlassian.dev.mode=false

package:
	@atlas-mvn package -q -T 4

nginx:
	sudo nginx -c $(shell readlink -f nginx.conf) -g 'daemon off;'

batchjs:
	@./buildutils/batchjs
