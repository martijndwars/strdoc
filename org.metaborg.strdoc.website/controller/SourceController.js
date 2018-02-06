app.controller('SourceViewController', function (baseUrl, $scope, $routeParams, $location, moduleService, $timeout, $anchorScroll) {
  var name = $routeParams.name;
  var hash = $location.hash();
  var range = getRange(hash);

  $scope.module = name.substring(0, name.length-9);

  moduleService.getSource(name).then(function (source) {
    $scope.source = source;

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
    if (hash == '') {
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
