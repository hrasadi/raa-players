module.exports = function(grunt) {
	// Project configuration.
	grunt.initConfig({
	  pkg: grunt.file.readJSON('package.json'),

	  uglify: {
	  	options: {
	  		sourceMap: true
		},
		uglify: {
			files: {
				'raa.min.js': ['raa.js', 'equalizer.js']
			}
		}
	  }
	});

 	grunt.loadNpmTasks('grunt-contrib-uglify');
 	grunt.registerTask('default', ['uglify']);
}