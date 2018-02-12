var app = angular.module('xdoc', ['ngRoute', 'ngMeta']);

app.value('baseUrl', window.location.origin + '/');

function Module(name, description, authors, see, strategies) {
  this.name = name;
  this.description = description;
  this.authors = authors;
  this.see = see;
  this.strategies = strategies;
}

function Strategy(name, description, types, params, notes, see, start, end) {
  this.name = name;
  this.description = description;
  this.types = types;
  this.params = params;
  this.notes = notes;
  this.see = see;
  this.start = start;
  this.end = end;
}

Strategy.prototype.lines = function () {
  if (this.start == this.end) {
    return this.start;
  }

  return this.start + '-' + this.end;
};

app.run(function($rootScope, $location, moduleService, ngMeta) {
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

  // Initiate ngMeta
  ngMeta.init();
});

app.filter('lines', function () {
  return function (source, strategy) {
    if (source == undefined) {
      return undefined;
    }

    var lines = source.split('\n');
    var subset = lines.slice(strategy.start, strategy.end+1);

    return subset.join('\n');
  }
});

app.filter('unsafe', function ($sce) {
  return $sce.trustAsHtml;
});

app.factory('moduleService', function (baseUrl, $http, $location, $templateCache, $q) {
  var getPackages = function () {
    return $http.get(baseUrl + 'data/json/packages.json').then(function (response) {
      return response.data;
    });
  };

  var getModules = function () {
    return $http.get(baseUrl + 'data/json/packages.json').then(function (response) {
      return _.flatMap(response.data, function (package) {
        return package.modules;
      });
    });
  };

  var getModule = function (name) {
    var file = baseUrl + 'data/json/' + name + '.str.json';

    return $http.get(file).then(function (response) {
      var data = response.data;

      // Fix the prototype of each strategy in the module
      data.strategies = data.strategies.map(function (strategy) {
        return Object.assign(new Strategy, strategy);
      });

      // Fix the prototype of the module
      return Object.assign(new Module, data);
    }).catch(function (error) {
      $location.url('/404').replace();
    });
  };

  var getModuleCached = function (name) {
    if ($templateCache.get(name) == undefined) {
      return getModule(name).then(function (module) {
        return $templateCache.put(name, module);
      });
    } else {
      return $q.when($templateCache.get(name));
    }
  };

  var getSource = function (name) {
    var file = baseUrl + 'data/source/' + name;

    return $http.get(file).then(function (response) {
      return response.data;
    }).catch(function (error) {
      $location.url('/404').replace();
    });
  }

  return {
    getPackages: getPackages,
    getModules: getModules,
    getModule: getModuleCached,
    getSource: getSource
  };
});

app.config(function ($routeProvider, $locationProvider) {
  $locationProvider.html5Mode(true);

  $routeProvider
    .when('/', {
      templateUrl: 'list.html'
    })
    .when('/package/:name*', {
      controller: 'PackageViewController',
      templateUrl: 'package.html'
    })
    .when('/module/:name*', {
      controller: 'ModuleViewController',
      templateUrl: 'module.html'
    })
    .when('/source/:name*', {
      controller: 'SourceViewController',
      templateUrl: 'source.html'
    })
    .when('/search/:query', {
      controller: 'SearchController',
      templateUrl: 'search.html'
    })
    .when('/404', {
      templateUrl: '404.html',
      data: {
        meta: {
          'prerender-status-code': '404'
        }
      }
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
