$('ul.packages ul li a').map(function () {
  var url = this.href;
  var source = url.substring(0, 32) + '/source/' + url.slice(40) + '.str.html';

  return $.get(url + '?_escaped_fragment_=', function () {
    console.log('Got ' + url);
  });

  return $.get(source + '?_escaped_fragment_=', function () {
    console.log('Got ' + source);
  });
}).toArray()
