
/*
 * GET home page.
 */

exports.index = function(req, res){
    res.render('api', { title: 'WebAPI Sample' })
};
