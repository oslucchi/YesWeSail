'use strict';

describe('Filter: noemaildomain', function () {

  // load the filter's module
  beforeEach(module('yeswesailApp'));

  // initialize a new instance of the filter before each test
  var noemaildomain;
  beforeEach(inject(function ($filter) {
    noemaildomain = $filter('noemaildomain');
  }));

  it('should return the input prefixed with "noemaildomain filter:"', function () {
    var text = 'angularjs';
    expect(noemaildomain(text)).toBe('noemaildomain filter: ' + text);
  });

});
