app.controller('SourceViewController', function (baseUrl, $scope, $routeParams, $location, $http, $sce, $timeout, $anchorScroll) {
  var name = $routeParams.name;
  var hash = $location.hash();
  var range = getRange(hash);

  $scope.module = name.substring(0, name.length-9);

  $http.get(baseUrl + 'data/source/' + name).then(function (response) {
    $scope.source = $sce.trustAsHtml(response.data);

    $timeout(function() {
      if (range == undefined) {
        return;
      }

      $anchorScroll(range.start);

      for (var i = parseInt(range.start); i <= parseInt(range.end); i++) {
        $('#' + i).addClass('highlight');
      }
    }, 0);
  });

  function getRange(hash) {
    if (hash == '' || hash == undefined) {
      return undefined;
    }

    if (hash.indexOf('-') == -1) {
      return {
        start: hash,
        end: hash
      };
    }

    var splits = hash.split('-');

    return {
      start: splits[0],
      end: splits[1]
    };
  }
});
