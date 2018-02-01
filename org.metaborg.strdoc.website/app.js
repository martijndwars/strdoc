var app = angular.module('xdoc', ['ngRoute']);

app.value('baseUrl', window.location.origin + '/');

function Module(name, description, authors, see, strategies) {
  this.name = name;
  this.description = description;
  this.authors = authors;
  this.see = see;
  this.strategies = strategies;
}

function Strategy(name, description, types, params, notes, start, end) {
  this.name = name;
  this.description = description;
  this.types = types;
  this.params = params;
  this.notes = notes;
  this.start = start;
  this.end = end;
}

Strategy.prototype.lines = function () {
  if (this.start == this.end) {
    return this.start;
  }

  return this.start + '-' + this.end;
};

app.run(function($rootScope, $location, moduleService) {
  $rootScope.query = '';

  $rootScope.search = function() {
    $location.path('/search/' + $rootScope.query);
  };

  // Reset the query when leaving the search page
  $rootScope.$on('$routeChangeStart', function (event, next, current) { 
    if (current == undefined) {
      return;
    }
    
    var currentRoute = current['$$route'];

    if (currentRoute && currentRoute.controller == 'SearchController') {
      var nextRoute = next['$$route'];

      if (nextRoute && nextRoute.controller != 'SearchController') {
        $rootScope.query = '';
      }
    }
  });

  // Populate right menu
  moduleService.getPackages().then(function (packages) {
    $rootScope.packages = packages;
  });
});

app.factory('moduleService', function (baseUrl, $http) {
  var getPackages = function () {
    return $http.get(baseUrl + 'data/packages.json').then(function (response) {
      return response.data;
    });
  };

  var getModules = function () {
    return $http.get(baseUrl + 'data/modules.json').then(function (response) {
      return response.data;
    });
  };

  var getModule = function (name) {
    var file = baseUrl + 'data/' + name + '.str.json';

    return $http.get(file).then(function (response) {
      var data = response.data;

      // Fix the prototype of each strategy in the module
      data.strategies = data.strategies.map(function (strategy) {
        return Object.assign(new Strategy, strategy);
      });

      // Fix the prototype of the module
      return Object.assign(new Module, data);
    }).catch(function (error) {
      console.error('Cannot load ' + name, error);
    });
  };

  return {
    getPackages: getPackages,
    getModules: getModules,
    getModule: getModule
  };
});

app.config(function ($routeProvider, $locationProvider) {
  $locationProvider.hashPrefix('');

  $routeProvider
    .when('/', {
      templateUrl: 'list.html'
    })
    .when('/package/:name*', {
      controller: 'PackageViewController',
      templateUrl: 'package.html'
    })
    .when('/module/:name*/strategy/:strategy*', {
      controller: 'ModuleViewController',
      templateUrl: 'module.html'
    })
    .when('/module/:name*', {
      controller: 'ModuleViewController',
      templateUrl: 'module.html'
    })
    .when('/source/:name*/:start-:end', {
      controller: 'SourceViewController',
      templateUrl: 'source.html'
    })
    .when('/source/:name*/:line', {
      controller: 'SourceViewController',
      templateUrl: 'source.html'
    })
    .when('/source/:name*', {
      controller: 'SourceViewController',
      templateUrl: 'source.html'
    })
    .when('/search/:query', {
      controller: 'SearchController',
      templateUrl: 'search.html'
    })
    .otherwise({
      redirectTo:'/'
    });
});

// Focus on search input when pressing 's'
$(document).on('keydown', function (e) {
  if (e.metaKey) {
    return;
  }

  var inputs = $('input');

  function focusOn(selector) {
    for (input in inputs) {
      if (inputs[input] == document.activeElement) {
        return true;
      }
    }

    $(selector).focus();

    return false;
  }

  if (e.keyCode == 83) {
    return focusOn('input#search');
  } else if (e.keyCode === 70) {
    return focusOn('input#filter');
  }
});
