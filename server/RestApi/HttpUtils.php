<?php
namespace RestApi;

class HttpUtils {
	
	public static function getProtocol(){
		return isset($_SERVER['SERVER_PROTOCOL'])
			? $_SERVER['SERVER_PROTOCOL']
			: 'HTTP/1.0';
	}
	
	public static function getHash($data){
		return md5(serialize($data));
	}

	public static function addHeader_ETag($data, $checkIfNoneMatch = false){
		$etag = self::getHash($data);
		
		if( $checkIfNoneMatch && isset($_SERVER['HTTP_IF_NONE_MATCH']) ){
			$noneMatch = trim($_SERVER['HTTP_IF_NONE_MATCH'], '"');
            
            if ($etag === $noneMatch) {
                header(self::getProtocol() . ' 304 Not Modified');
                return false;
            }
		}
		
		header('ETag: "' . $etag . '"');
		return true;
	}
	
	public static function addHeader_LastModified($mtime, $checkIfModifiedSince = false){
		if($checkIfModifiedSince && isset($_SERVER['HTTP_IF_MODIFIED_SINCE'])){
			$modifiedSince = $_SERVER['HTTP_IF_MODIFIED_SINCE'];
			if (false !== ($semicolonPos = strpos($modifiedSince, ';'))) {
				$modifiedSince = substr($modifiedSince, 0, $semicolonPos);
			}
			
            if ($mtime === @strtotime($modifiedSince)) {
                header(self::getProtocol() . ' 304 Not Modified');
                return false;
            }
		}
		
		$lastModified = gmdate('D, d M Y H:i:s', $mtime) . ' GMT';
		header('Last-Modified: ' . $lastModified);
		return true;
	}
	
	public static function addHeader_expires($livePeriod){
		$expires = gmdate('D, d M Y H:i:s', time() + $livePeriod) . ' GMT';
		header('Expires: ' . $expires);
		header('Cache-Control: max-age=' . $livePeriod . ', public, must-revalidate');
		return true;
	}
}
