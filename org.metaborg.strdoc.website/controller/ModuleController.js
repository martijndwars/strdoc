app.controller('ModuleListController', function ($scope, moduleService) {
  moduleService.getModules().then(function (modules) {
    $scope.modules = modules;
  });
});

app.controller('ModuleViewController', function ($scope, $routeParams, $timeout, $anchorScroll, moduleService) {
  var moduleName = $routeParams.name;

  moduleService.getModule(moduleName).then(function (module) {
    $scope.module = module;

    if ($routeParams.strategy != undefined) {
      $timeout(function() {
        $anchorScroll($routeParams.strategy);
      }, 0);
    }
  });
});
