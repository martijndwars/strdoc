app.controller('PackageViewController', function ($scope, $routeParams, moduleService) {
  var name = $routeParams.name;

  moduleService.getPackages().then(function (packages) {
    var package = packages.find(function (package) {
      if (package.name == name) {
        return package;
      }
    })

    $scope.package = package;
  });
});
