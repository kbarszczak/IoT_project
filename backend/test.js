const AWS = require('aws-sdk');
const util = require('./utils/util');
const auth = require('./utils/auth')

require('dotenv').config();
AWS.config.update({
    region: 'eu-central-1',
    accessKeyId: process.env.AWS_ACCESS_KEY_ID,
    secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY
})

const dynamodb = new AWS.DynamoDB.DocumentClient();
const userTable = 'stm32-users';
const dataTable = 'stm32-data';




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
    let data = await dynamodb.get(params).promise().then(response => {
        console.log(response.Item)
        return response.Item;
    }, error => {
        console.error('There is an error getting user: ', error)
    })

    if (data.devices) {
        if (!data.devices.includes(deviceId)) {
            return util.buildResponse(402, {
                message: 'Dont be a scammer, its not your device...'
            })
        }
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


console.log(getDeviceData("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImJsYWJsYSIsIm5hbWUiOiJibGFibGEiLCJpYXQiOjE2NzQ2OTk3NjksImV4cCI6MTY3NDcwMzM2OX0.6GLot2K99CGQN_xDbU7TlC5FuvWeRtCneabkIT9yWnc", { deviceId: "32" }).then(res => console.log(res)))