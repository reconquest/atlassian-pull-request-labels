package:
	@atlas-mvn package -q -T 4

nginx:
	sudo nginx -c $(shell readlink -f nginx.conf) -g 'daemon off;'

batchjs:
	@./buildutils/batchjs
