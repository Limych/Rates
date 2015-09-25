<?php
use Loader;
define('DEBUG', true);	// Uncomment for debug mode

if (defined('DEBUG')){
	error_reporting(E_ALL);
	ini_set( 'display_errors', 1 );
} else {
	define('DEBUG', false);
}

$loader = include __DIR__ . '/vendor/autoload.php';
$loader->add('Loader', __DIR__ . '/');
$loader->add('Model', __DIR__ . '/');
$loader->add('RestApi', __DIR__ . '/');

// $stockItems = Loader\MoexLoader::create()->load();
$stockItems = Loader\CbrfLoader::create()->load();

RestService\Server::create('/api', new RestApi\Rates)
//     ->setDebugMode(true) //prints the debug trace, line number and file if a exception has been thrown.
	->collectRoutes()
	->run();

# http://4map.ru/service/get_rates_to_map.aspx?country=RU&latsw=55.56080032284363&lngsw=37.261019780273514&latne=55.77997579045521&lngne=37.933932377929764&zoom=12&curr1=RUB&curr2=USD&amount=1000&rate_left=-1&rate_right=-1&ww=1280&wh=616&src=1
# http://www.bmsk.ru/