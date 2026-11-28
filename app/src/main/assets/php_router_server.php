<?php

const WTA_ROUTER_DIAGNOSTIC_LIMIT = 65536;

const WTA_ROUTER_RANGE_CHUNK = 2097152;

const WTA_ROUTER_CONTENT_TYPES = [
    'mp3' => 'audio/mpeg', 'm4a' => 'audio/mp4', 'aac' => 'audio/aac', 'wav' => 'audio/wav',
    'ogg' => 'audio/ogg', 'oga' => 'audio/ogg', 'opus' => 'audio/ogg', 'flac' => 'audio/flac',
    'weba' => 'audio/webm', 'mp4' => 'video/mp4', 'm4v' => 'video/mp4', 'webm' => 'video/webm',
    'ogv' => 'video/ogg', 'mov' => 'video/quicktime', 'mkv' => 'video/x-matroska',
    'html' => 'text/html; charset=UTF-8', 'htm' => 'text/html; charset=UTF-8', 'css' => 'text/css',
    'js' => 'text/javascript', 'mjs' => 'text/javascript', 'json' => 'application/json',
    'xml' => 'application/xml', 'txt' => 'text/plain; charset=UTF-8', 'csv' => 'text/csv',
    'png' => 'image/png', 'jpg' => 'image/jpeg', 'jpeg' => 'image/jpeg', 'gif' => 'image/gif',
    'svg' => 'image/svg+xml', 'webp' => 'image/webp', 'avif' => 'image/avif', 'ico' => 'image/x-icon',
    'woff' => 'font/woff', 'woff2' => 'font/woff2', 'ttf' => 'font/ttf', 'otf' => 'font/otf',
    'pdf' => 'application/pdf', 'zip' => 'application/zip', 'wasm' => 'application/wasm',
    'map' => 'application/json',
];

const WTA_ROUTER_STATIC_EXTENSIONS = [
    'css', 'js', 'mjs', 'json', 'xml', 'txt', 'csv', 'png', 'jpg', 'jpeg', 'gif', 'svg', 'ico',
    'webp', 'avif', 'woff', 'woff2', 'ttf', 'otf', 'pdf', 'zip', 'mp4', 'webm', 'mp3', 'ogg', 'wav',
    'map', 'wasm',
];

function wta_router_entry_file(): string
{
    $entry = getenv('WEBTOAPP_PHP_ENTRY_FILE');
    return is_string($entry) && $entry !== '' ? $entry : 'index.php';
}

function wta_router_resolve(string $path, string $docRoot, string $entryFile): string
{
    $path = '/' . ltrim(str_replace('\\', '/', $path), '/');
    if (strpos($path, '..') !== false || strpos($path, "\0") !== false) {
        return '';
    }

    $filePath = rtrim($docRoot . $path, '/');
    if (is_dir($filePath)) {
        foreach (['index.php', 'index.html', 'index.htm'] as $index) {
            $candidate = $filePath . '/' . $index;
            if (is_file($candidate)) {
                return $candidate;
            }
        }
    }

    if (is_file($filePath)) {
        return $filePath;
    }

    $extension = strtolower(pathinfo($path, PATHINFO_EXTENSION));
    if ($extension !== '' && in_array($extension, WTA_ROUTER_STATIC_EXTENSIONS, true)) {
        return $filePath;
    }

    return $docRoot . '/' . ltrim($entryFile, '/');
}

function wta_router_file_content_type(string $file): string
{
    $extension = strtolower(pathinfo($file, PATHINFO_EXTENSION));
    if (isset(WTA_ROUTER_CONTENT_TYPES[$extension])) {
        return WTA_ROUTER_CONTENT_TYPES[$extension];
    }
    if (function_exists('mime_content_type')) {
        $detected = @mime_content_type($file);
        if (is_string($detected) && $detected !== '') {
            return $detected;
        }
    }
    return 'application/octet-stream';
}

function wta_router_parse_range(string $header, int $size): array|false|null
{
    if (!preg_match('/^\s*bytes\s*=\s*(\d*)\s*-\s*(\d*)\s*$/i', $header, $m)) {
        return null;
    }
    if ($m[1] === '' && $m[2] === '') {
        return null;
    }
    if ($m[1] === '') {
        $suffix = (int) $m[2];
        if ($suffix === 0 || $size === 0) {
            return false;
        }
        return [max(0, $size - $suffix), $size - 1, false];
    }
    $start = (int) $m[1];
    if ($start >= $size) {
        return false;
    }
    if ($m[2] === '') {
        return [$start, $size - 1, true];
    }
    $end = (int) $m[2];
    if ($end < $start) {
        return null;
    }
    return [$start, min($end, $size - 1), false];
}

function wta_router_serve_range(string $file): void
{
    $size = (int) filesize($file);
    $modified = (int) filemtime($file);
    $etag = sprintf('"%x-%x"', $modified, $size);
    $lastModified = gmdate('D, d M Y H:i:s', $modified) . ' GMT';

    $range = wta_router_parse_range((string) ($_SERVER['HTTP_RANGE'] ?? ''), $size);
    $ifRange = $_SERVER['HTTP_IF_RANGE'] ?? null;
    if ($ifRange !== null && trim($ifRange) !== $etag && trim($ifRange) !== $lastModified) {
        $range = null;
    }

    header('Accept-Ranges: bytes');
    header('ETag: ' . $etag);
    header('Last-Modified: ' . $lastModified);

    if ($range === false) {
        http_response_code(416);
        header('Content-Range: bytes */' . $size);
        header('Content-Length: 0');
        return;
    }

    header('Content-Type: ' . wta_router_file_content_type($file));
    if ($range === null) {
        $start = 0;
        $end = $size - 1;
        http_response_code(200);
    } else {
        [$start, $end, $openEnded] = $range;
        if ($openEnded) {
            $end = min($end, $start + WTA_ROUTER_RANGE_CHUNK - 1);
        }
        http_response_code(206);
        header("Content-Range: bytes $start-$end/$size");
    }
    $length = $size === 0 ? 0 : $end - $start + 1;
    header('Content-Length: ' . $length);

    if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'HEAD' || $length === 0) {
        return;
    }

    $handle = fopen($file, 'rb');
    if ($handle === false) {
        return;
    }
    fseek($handle, $start);
    $remaining = $length;
    while ($remaining > 0 && !feof($handle)) {
        $chunk = fread($handle, min(65536, $remaining));
        if ($chunk === false || $chunk === '') {
            break;
        }
        echo $chunk;
        flush();
        $remaining -= strlen($chunk);
        if (connection_aborted()) {
            break;
        }
    }
    fclose($handle);
}

function wta_router_content_type(): string
{
    return strtolower($_SERVER['CONTENT_TYPE'] ?? $_SERVER['HTTP_CONTENT_TYPE'] ?? '');
}

function wta_router_prepare_request_body(): void
{
    $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
    $contentType = wta_router_content_type();
    $isMultipart = str_contains($contentType, 'multipart/form-data');
    $isUrlEncoded = str_contains($contentType, 'application/x-www-form-urlencoded');
    $requestChanged = false;
    $parseBody = in_array($method, ['PUT', 'PATCH'], true) && ($isMultipart || $isUrlEncoded) && $_POST === [] && $_FILES === [];

    if ($parseBody && function_exists('request_parse_body')) {
        try {
            [$_POST, $_FILES] = request_parse_body();
            $requestChanged = true;
        } catch (\Throwable $e) {
            error_log('[RouterServer] request_parse_body failed: ' . $e->getMessage());
        }
    }

    $GLOBALS['__HTTP_RAW_BODY'] = $isMultipart ? '' : (string) file_get_contents('php://input');

    if ($parseBody && !$requestChanged && $isUrlEncoded) {
        parse_str($GLOBALS['__HTTP_RAW_BODY'], $_POST);
        $requestChanged = true;
    }

    if (in_array($method, ['POST', 'PUT', 'PATCH'], true)
        && str_contains($contentType, 'application/json')
        && $_POST === []
        && $GLOBALS['__HTTP_RAW_BODY'] !== ''
    ) {
        $decoded = json_decode($GLOBALS['__HTTP_RAW_BODY'], true);
        if (is_array($decoded)) {
            $_POST = $decoded;
            $requestChanged = true;
        }
    }

    if ($requestChanged) {
        $_REQUEST = array_merge($_GET, $_POST, $_COOKIE);
    }
}

function wta_router_is_fatal(?array $error): bool
{
    return $error !== null && in_array(
        $error['type'] ?? 0,
        [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR, E_USER_ERROR],
        true
    );
}

function wta_router_finalize(): void
{
    if (function_exists('wta_session_polyfill_flush')) {
        wta_session_polyfill_flush();
    }

    if (headers_sent()) {
        return;
    }

    $error = error_get_last();
    $fatal = wta_router_is_fatal($error);
    $code = http_response_code() ?: 200;
    if (!$fatal && $code < 500) {
        return;
    }

    $output = '';
    while (ob_get_level() > 0) {
        $output = ob_get_clean() . $output;
    }

    if ($fatal) {
        $message = ($error['message'] ?? 'Unknown fatal error')
            . ' in ' . ($error['file'] ?? 'unknown')
            . ':' . ($error['line'] ?? 0);
        http_response_code(500);
        if ($output === '') {
            $output = '<h1>500 Internal Server Error</h1><pre>' . htmlspecialchars($message) . '</pre>';
        }
    }

    if ($output !== '' && strlen($output) <= WTA_ROUTER_DIAGNOSTIC_LIMIT) {
        header('X-Wta-Router-Error: ' . base64_encode($output));
    }
    echo $output;
}

if (!function_exists('session_start')) {
    $GLOBALS['__session_file'] = null;
    $GLOBALS['__session_id'] = '';
    $GLOBALS['__session_started'] = false;
    $GLOBALS['__session_name'] = 'PHPSESSID';

    function wta_session_polyfill_flush(): void
    {
        if ($GLOBALS['__session_started'] ?? false) {
            session_write_close();
        }
    }

    function session_start(array $options = []): bool
    {
        if ($GLOBALS['__session_started']) {
            return true;
        }

        $sessionDir = ($GLOBALS['__session_doc_root'] ?? '.') . '/.sessions';
        if (!is_dir($sessionDir)) {
            @mkdir($sessionDir, 0700, true);
        }

        $name = $GLOBALS['__session_name'];
        $sid = $_COOKIE[$name] ?? '';
        if (!is_string($sid) || !preg_match('/^[a-zA-Z0-9,-]{22,256}$/', $sid)) {
            $sid = bin2hex(random_bytes(16));
            header("Set-Cookie: $name=$sid; Path=/; HttpOnly; SameSite=Lax", false);
        }

        $GLOBALS['__session_id'] = $sid;
        $GLOBALS['__session_file'] = $sessionDir . '/sess_' . $sid;

        $GLOBALS['_SESSION'] = [];
        if (is_file($GLOBALS['__session_file'])) {
            $data = @file_get_contents($GLOBALS['__session_file']);
            if ($data !== false) {
                $decoded = json_decode($data, true);
                if (is_array($decoded)) {
                    $GLOBALS['_SESSION'] = $decoded;
                }
            }
        }

        $GLOBALS['__session_started'] = true;
        return true;
    }

    function session_write_close(): bool
    {
        if (!$GLOBALS['__session_started']) {
            return true;
        }
        if ($GLOBALS['__session_file']) {
            @file_put_contents(
                $GLOBALS['__session_file'],
                json_encode($GLOBALS['_SESSION'] ?? [], JSON_UNESCAPED_UNICODE),
                LOCK_EX
            );
        }
        $GLOBALS['__session_started'] = false;
        return true;
    }

    function session_commit(): bool
    {
        return session_write_close();
    }

    function session_destroy(): bool
    {
        $GLOBALS['_SESSION'] = [];
        if ($GLOBALS['__session_file'] && is_file($GLOBALS['__session_file'])) {
            @unlink($GLOBALS['__session_file']);
        }
        $GLOBALS['__session_started'] = false;
        return true;
    }

    function session_id(string $id = ''): string|false
    {
        if ($id !== '') {
            $GLOBALS['__session_id'] = $id;
        }
        return $GLOBALS['__session_id'];
    }

    function session_name(string $name = ''): string|false
    {
        $old = $GLOBALS['__session_name'];
        if ($name !== '') {
            $GLOBALS['__session_name'] = $name;
        }
        return $old;
    }

    function session_status(): int
    {
        return $GLOBALS['__session_started'] ? PHP_SESSION_ACTIVE : PHP_SESSION_NONE;
    }

    function session_regenerate_id(bool $delete_old = false): bool
    {
        if (!$GLOBALS['__session_started']) {
            return false;
        }
        $oldFile = $GLOBALS['__session_file'];
        $sid = bin2hex(random_bytes(16));
        $GLOBALS['__session_id'] = $sid;
        $name = $GLOBALS['__session_name'];
        header("Set-Cookie: $name=$sid; Path=/; HttpOnly; SameSite=Lax", false);
        $GLOBALS['__session_file'] = ($GLOBALS['__session_doc_root'] ?? '.') . '/.sessions/sess_' . $sid;
        if ($delete_old && $oldFile && is_file($oldFile)) {
            @unlink($oldFile);
        }
        return true;
    }

    function session_unset(): bool
    {
        $GLOBALS['_SESSION'] = [];
        return true;
    }
}

if (PHP_SAPI !== 'cli-server') {
    fwrite(STDERR, "[RouterServer] ERROR: must run under the built-in web server: php -S 127.0.0.1:<port> -t <docroot> " . __FILE__ . "\n");
    exit(1);
}

$GLOBALS['__session_doc_root'] = rtrim((string) $_SERVER['DOCUMENT_ROOT'], '/');

if (rawurldecode((string) parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH)) === '/__health') {
    header('Content-Type: text/plain');
    echo 'OK';
    return true;
}

$GLOBALS['__wta_router_target'] = wta_router_resolve(
    rawurldecode((string) parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH)),
    $GLOBALS['__session_doc_root'],
    wta_router_entry_file()
);

if ($GLOBALS['__wta_router_target'] === '' || !is_file($GLOBALS['__wta_router_target'])) {
    http_response_code(404);
    header('Content-Type: text/plain');
    echo 'Not Found';
    return true;
}

if (strtolower(pathinfo($GLOBALS['__wta_router_target'], PATHINFO_EXTENSION)) !== 'php') {
    if (isset($_SERVER['HTTP_RANGE']) && in_array($_SERVER['REQUEST_METHOD'] ?? 'GET', ['GET', 'HEAD'], true)) {
        wta_router_serve_range($GLOBALS['__wta_router_target']);
        return true;
    }
    return false;
}

$_SERVER['SCRIPT_FILENAME'] = $GLOBALS['__wta_router_target'];
$_SERVER['SCRIPT_NAME'] = '/' . ltrim(substr($GLOBALS['__wta_router_target'], strlen($GLOBALS['__session_doc_root'])), '/');
$_SERVER['PHP_SELF'] = $_SERVER['SCRIPT_NAME'];
unset($GLOBALS['__wta_router_target']);

wta_router_prepare_request_body();

register_shutdown_function(static function (): void {
    register_shutdown_function('wta_router_finalize');
});

chdir($_SERVER['DOCUMENT_ROOT']);
ob_start(null, WTA_ROUTER_DIAGNOSTIC_LIMIT);

require $_SERVER['SCRIPT_FILENAME'];

return true;
