# Upload everything to the server
#scp -r * root@146.185.154.205:/var/www/stratego.martijndwars.nl

# Upload only changes to the server
rsync -avh org.metaborg.strdoc.website/* root@146.185.154.205:/var/www/stratego.martijndwars.nl
