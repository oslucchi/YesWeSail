'use strict';

describe('Service: CATEGORIES', function () {

  // load the service's module
  beforeEach(module('yeswesailApp'));

  // instantiate service
  var CATEGORIES;
  beforeEach(inject(function (_CATEGORIES_) {
    CATEGORIES = _CATEGORIES_;
  }));

  it('should do something', function () {
    expect(!!CATEGORIES).toBe(true);
  });

});
