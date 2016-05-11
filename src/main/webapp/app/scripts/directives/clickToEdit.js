angular.module('yeswesailApp')
.directive("clickToEdit", function () {
    var editorTemplate = '' +
        '<div class="click-to-edit">' +
            '<div ng-hide="view.editorEnabled">' +
                '<span ng-click="enableEditor()">{{value}}</span>' +
                '<button ng-if="!value" class="ui mini button" ng-click="enableEditor()">Edit</button>' +
            '</div>' +
            '<div ng-show="view.editorEnabled">' +
                '<div class="ui action input"><input type="text"  ng-model="view.editableValue">'+
        '<div type="submit"  ng-click="save()" class="ui primary button">Save</div><div type="submit" ng-click="disableEditor()" class="ui button">Cancel</div></div>' +
            '</div>' +
        '</div>';

    
    
    
             
  
  
    return {
        restrict: "A",
        replace: true,
        template: editorTemplate,
        scope: {
            value: "=clickToEdit",
        },
        link: function (scope, element, attrs) {
            scope.view = {
                editableValue: scope.value,
                editorEnabled: false
            };

            scope.enableEditor = function () {
                scope.view.editorEnabled = true;
                scope.view.editableValue = scope.value;
                setTimeout(function () {
                    element.find('input')[0].focus();
                    //element.find('input').focus().select(); // w/ jQuery
                });
            };

            scope.disableEditor = function () {
                scope.view.editorEnabled = false;
            };

            scope.save = function () {
                scope.value = scope.view.editableValue;
                scope.disableEditor();
            };

        }
    };
});
