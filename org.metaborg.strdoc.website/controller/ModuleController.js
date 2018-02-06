app.controller('ModuleViewController', function ($scope, $routeParams, $location, $timeout, $anchorScroll, $q, moduleService) {
  var name = $routeParams.name;
  var hash = $location.hash();

  var getModule = moduleService.getModule(name);
  var getSource = moduleService.getSource(name + '.str.html');

  $q.all([getModule, getSource]).then(function (results) {
    $scope.source = results[1];
    $scope.module = results[0];

    if (hash != '') {
      $timeout(function() {
        $anchorScroll(hash);
      }, 0);
    }
  });
});
