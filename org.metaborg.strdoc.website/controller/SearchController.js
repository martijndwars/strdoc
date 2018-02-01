app.controller('SearchController', function ($scope, $routeParams, $q, moduleService) {
  var query = $routeParams.query.toLowerCase();
  
  moduleService.getModules().then(function (modules) {
    var modules = modules.map(function (name) {
      return moduleService.getModule(name);
    });

    $q.all(modules).then(function (modules) {
      var allStrategies = _.flatMap(modules, function (module) {
        return module.strategies.map(function (strategy) {
          return angular.extend({
            module: module
          }, strategy);
        });
      });

      var filteredStrategies = allStrategies.filter(function (strategy) {
        return strategy.name.toLowerCase().indexOf(query) != -1;
      });

      $scope.strategies = filteredStrategies;
    });
  });
});
