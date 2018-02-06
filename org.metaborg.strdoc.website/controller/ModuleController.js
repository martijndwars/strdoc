app.controller('ModuleViewController', function ($scope, $routeParams, $location, $timeout, $anchorScroll, moduleService) {
  var name = $routeParams.name;
  var hash = $location.hash();

  moduleService.getModule(name).then(function (module) {
    $scope.module = module;

    if (hash != '') {
      $timeout(function() {
        $anchorScroll(hash);
      }, 0);
    }
  });
});
