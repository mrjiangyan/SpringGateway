'use strict';

var fs = require('fs'),
    gulp = require('gulp'),
    lazypipe = require('lazypipe'),
    footer = require('gulp-footer'),
    sourcemaps = require('gulp-sourcemaps'),
    rev = require('gulp-rev'),
    htmlmin = require('gulp-htmlmin'),
    ngAnnotate = require('gulp-ng-annotate'),
    prefix = require('gulp-autoprefixer'),
    cssnano = require('gulp-cssnano'),
    uglify = require('gulp-uglify'),
    useref = require("gulp-useref"),
    revReplace = require("gulp-rev-replace"),
    plumber = require('gulp-plumber'),
    gulpIf = require('gulp-if'),
    handleErrors = require('./handle-errors');

var config = require('./config');

var initTask = lazypipe()
    .pipe(sourcemaps.init);
var jsTask = lazypipe()
    .pipe(ngAnnotate)
    .pipe(uglify);
var cssTask = lazypipe()
    .pipe(prefix)
    .pipe(cssnano);

