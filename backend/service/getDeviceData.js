const AWS = require('aws-sdk');
const util = require('../utils/util');
const auth = require('../utils/auth')


AWS.config.update({
    region: 'eu-central-1',
})

const dynamodb = new AWS.DynamoDB.DocumentClient();
const dataTable = 'stm32-data';
const userTable = 'stm32-users';

const getDeviceData = async (userToken, body) => {

    deviceId = body.deviceId;
    if (!deviceId) {
        return util.buildResponse(401, {
            message: 'Device id not provided.'
        })
    }

    if (!userToken) {
        return util.buildResponse(401, {
            message: 'Please insert the userToken'
        })
    }

    if (await auth.verifyTokenNoUser(userToken).verified == false) {
        return util.buildResponse(402, {
            message: 'Invalid token'
        })
    }

    let username = await auth.getUserFromToken(userToken)

    const params = {
        TableName: userTable,
        Key: {
            username: username
        },
    }
    console.log(username)
    let data = await dynamodb.get(params).promise().then(response => {
        console.log(response.Item)
        return response.Item;
    }, error => {
        console.error('There is an error getting user: ', error)
    })

    if (data && data.devices) {
        if (!data.devices.includes(deviceId)) {
            return util.buildResponse(402, {
                message: 'Dont be a scammer, its not your device...'
            })
        }
    } else {
        return util.buildResponse(402, {
            message: "You haven't registered any device yet."
        })
    }

    const scanParamsDevices = {
        TableName: dataTable,
        ExpressionAttributeNames: {
            "#S": "device_id",
            "#W": "device_data",
            "#A": "sample_time"
        },
        ExpressionAttributeValues: {
            ":a": +deviceId
        },
        FilterExpression: "device_id = :a",
        ProjectionExpression: "#S,#W,#A",
    };

    let scanData = await dynamodb.scan(scanParamsDevices).promise().then(response => {
        return response.Items;
    }, error => {
        console.error('There is an error getting user: ', error)
    })
    let latestData = null
    if (scanData.length > 0) {
        latestData = scanData.reduce((prev, current) => {
            return (prev.sample_time > current.sample_time) ? prev : current
        })
    }
    if (latestData) {
        const response = {
            device_data: latestData.device_data,
            device_id: latestData.device_id,
            sample_time: latestData.sample_time
        }
        return util.buildResponse(200, response)
    } else {
        return util.buildResponse(400, { message: "No data devices for your account has been found." })
    }
}

module.exports.getDeviceData = getDeviceData;