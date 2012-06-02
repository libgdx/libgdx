# fetch ikvm distri
curl "http://netcologne.dl.sourceforge.net/project/ikvm/ikvm/7.0.4335.0/ikvmbin-7.0.4335.0.zip" > ikvmbin.zip
unzip ikvmbin.zip
mv ikvm-7.0.4335.0 ikvm
rm ikvmbin.zip