FROM mockserver/mockserver

COPY ./initialize.sh /initialize.sh

RUN chmod +x /initialize.sh

CMD ["/initialize.sh"]