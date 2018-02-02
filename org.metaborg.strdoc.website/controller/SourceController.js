app.controller('SourceViewController', function (baseUrl, $scope, $routeParams, $http, $sce, $timeout, $anchorScroll) {
  var name = $routeParams.name;

  $scope.module = name.substring(0, name.length-9);

  $http.get(baseUrl + 'source/' + name).then(function (response) {
    $scope.source = $sce.trustAsHtml(response.data);

    var line = $routeParams.line;
    var start = $routeParams.start;
    var end = $routeParams.end;

    $timeout(function() {
      if (line != undefined) {
        // Scroll to the line
        $anchorScroll(line);

        // Highlight the line
        $('#' + line).addClass('highlight');
      } else if (start != undefined && end != undefined) {
        // Scroll to the start
        $anchorScroll(start);

        // Highlight the range
        for (var i = parseInt(start); i <= parseInt(end); i++) {
          $('#' + i).addClass('highlight');
        }
      }
    }, 0);
  });
});
