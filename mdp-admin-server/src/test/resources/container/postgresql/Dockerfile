FROM bitnine/agensgraph:v2.1.1

USER root
RUN yum install -y make git bzip2
RUN curl -L http://www.xunsearch.com/scws/down/scws-1.2.3.tar.bz2 | tar xjf -
RUN cd scws-1.2.3 \
    && ./configure \
    && make install
WORKDIR /
RUN git clone https://github.com/amutu/zhparser.git
RUN cd zhparser \
    && make && make install

USER agens
ENTRYPOINT ["/home/agens/scripts/entrypoint.sh"]}