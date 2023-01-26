const AWS = require('aws-sdk');
const auth = require('../utils/auth')
const util = require('../utils/util');
AWS.config.update({
    region: 'eu-central-1'
})

const dynamodb = new AWS.DynamoDB.DocumentClient();
const userTable = 'stm32-users';
const userSecret = "stm32-secrets"

const setDevice = async (body, userToken) => {


    deviceId = body.deviceId;
    secret = body.secret;
    if (!deviceId || !secret) {
        return util.buildResponse(401, {
            message: 'Device id and secret are required!!.'
        })
    }
    else if (!userToken) {
        return util.buildResponse(401, {
            message: 'Please insert the userToken'
        })
    }

    if (await auth.verifyTokenNoUser(userToken).verified == false) {
        return util.buildResponse(402, {
            message: 'Invalid token'
        })
    }

    const checkSecret = {
        TableName: userSecret,
        Key: {
            device_id: deviceId,
        },
    }
    let secretItem = await dynamodb.get(checkSecret).promise().then(response => {
        console.log(response.Item)
        return response.Item;
    }, error => {
        console.error('There is an error checking secret: ', error)
        return util.buildResponse(401, {
            message: 'Secret error'
        })
    })

    if (secretItem) {
        if (secretItem.device_id.length > 0) {
            if (!(secretItem.device_id == deviceId && secretItem.secret == secret)) {
                return util.buildResponse(401, {
                    message: 'Please provide right secret for this device.'
                })
            }
        } else {
            return util.buildResponse(401, {
                message: 'Please provide right device id.'
            })
        }
    } else {
        return util.buildResponse(401, {
            message: 'Please provide right device id and secret.'
        })
    }

    let username = await auth.getUserFromToken(userToken)

    const params = {
        TableName: userTable,
        Key: {
            username: username
        },
    }
    let data = await dynamodb.get(params).promise().then(response => {
        console.log(response.Item)
        return response.Item;
    }, error => {
        console.error('There is an error getting user: ', error)
    })

    const scanParamsDevices = {
        TableName: userTable,
        ExpressionAttributeNames: {
            "#S": "devices"
        },
        Select: "SPECIFIC_ATTRIBUTES",
        ProjectionExpression: "#S",
    };

    let scanData = await dynamodb.scan(scanParamsDevices).promise().then(response => {
        return response.Items;
    }, error => {
        console.error('There is an error getting user: ', error)
    })

    let usersDeviceIDs = new Set();
    scanData.map((el) => {
        if (Array.isArray(el.devices)) el.devices.map((elInside) => { usersDeviceIDs.add(elInside) })
    })

    if (usersDeviceIDs.has(deviceId)) {
        if (data.devices) {
            if (!data.devices.includes(deviceId)) {
                return util.buildResponse(401, {
                    message: 'Device id already exists'
                })
            }
        } else {
            return util.buildResponse(401, {
                message: 'Device id already exists'
            })
        }
    }
    if (data.devices) {
        if (!data.devices.includes(deviceId))
            data.devices.push(deviceId)
        else {

        }
    } else {
        data.devices = [deviceId]
    }
    params.Item = data
    dynamodb.put(params).promise().then(response => {
        console.log(response)
        return response.Item;
    }, error => {
        console.error('There is an error getting user: ', error)
    })
    return util.buildResponse(200, {
        message: 'Success'
    })
}

module.exports.setDevice = setDevice;