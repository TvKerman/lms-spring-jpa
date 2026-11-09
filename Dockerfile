FROM ubuntu:latest
LABEL authors="perfi"

ENTRYPOINT ["top", "-b"]