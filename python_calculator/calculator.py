"""
{
    "operation": [add|sub|mul|div],
    "operand1": 1,
    "operand2": 2
}
"""
import json
from operator import *

OPS = {
    'add': add,
    'sub': sub,
    'mul': mul,
    'div': truediv
}


def lambda_handler(event, context):
    body = json.loads(event['body'])

    return {
        "statusCode": 200,
        "headers": {
            "Content-Type": "application/json"
        },
        "isBase64Encoded": False,
        "body": OPS[body['operation']](
            float(body['operand1']),
            float(body['operand2'])
        )
    }
