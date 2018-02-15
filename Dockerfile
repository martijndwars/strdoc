FROM nginx

RUN rm /etc/nginx/conf.d/default.conf

COPY org.metaborg.strdoc.website /usr/share/nginx/html

COPY default.conf /etc/nginx/conf.d

