const registerService = require('./service/register');
const loginService = require('./service/login');
const verifyService = require('./service/verify');
const setDeviceService = require('./service/setDevice');
const getDeviceDataService = require('./service/getDeviceData');
const getDevicesIdService = require('./service/getDevicesId');
const util = require('./utils/util');

const loginPath = "/login"
const registerPath = "/register"
const verifyPath = "/verify"
const healthPath = "/health"
const setDevicePath = '/device'
const getDeviceDataPath = '/device-data'
const getDevicesIdPath = '/devices'

exports.handler = async (event) => {
    console.log("Request Event: ", event)
    let response;
    switch (true) {
        case event.httpMethod === "GET" && event.path === healthPath:
            response = util.buildResponse(200);
            break;
        case event.httpMethod === "POST" && event.path === registerPath:
            const registerBody = JSON.parse(event.body);
            response = await registerService.register(registerBody)
            break;
        case event.httpMethod === "POST" && event.path === loginPath:
            console.log(event.body)
            const loginBody = JSON.parse(event.body);
            response = await loginService.login(loginBody)
            break;
        case event.httpMethod === "POST" && event.path === verifyPath:
            const verifyBody = JSON.parse(event.body);
            response = await verifyService.verify(verifyBody)
            break;
        case event.httpMethod === "POST" && event.path === setDevicePath:
            const setDeviceBody = JSON.parse(event.body);
            response = await setDeviceService.setDevice(setDeviceBody, event.headers.Authorization)
            break;
        case event.httpMethod === "GET" && event.path === getDevicesIdPath:
            response = await getDevicesIdService.getDevices(event.headers.Authorization)
            break;
        case event.httpMethod === "POST" && event.path === getDeviceDataPath:
            const getDeviceDataBody = JSON.parse(event.body);
            response = await getDeviceDataService.getDeviceData(event.headers.Authorization, getDeviceDataBody)
            break;
        default:
            response = util.buildResponse(404, '404 Not Found');
    }
    return response;
}
