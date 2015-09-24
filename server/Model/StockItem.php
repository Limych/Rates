<?php
namespace Model;

class StockItem{

	var $source;
	var $code;
	var $faceValue;
	var $currency;
	var $open;
	var $last;
	var $high;
	var $low;
	var $timestamp;
	
	function __construct($initial = null){
		if( gettype($initial) === 'array' || gettype($initial) === 'object' ){
			foreach ((array) $initial as $key => $val){
				if (!empty($key))
					$this->$key = $val;
			}
		}
	}
}
