resource "aws_lambda_function" "my_func" {
  function_name = var.function_name
  handler       = var.function_handler
  role          = aws_iam_role.iam_for_lambda.arn
  runtime       = var.function_runtime
  s3_bucket     = var.function_s3_bucket
  s3_key        = var.function_s3_key
  memory_size   = var.function_memory_size
  depends_on    = [var.function_depends_on]
}

// https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/ScheduledEvents.html
// cron(*/10 * * * ? *) => every 10 minutes
// rate(10 minutes) => every 10 minutes
resource "aws_cloudwatch_event_rule" "every_10_min" {
  name                = "every-10-minutes"
  description         = "Fire the lambda function every 10 min"
  schedule_expression = "rate(10 minutes)"
  depends_on = [aws_lambda_function.my_func]
}

resource "aws_cloudwatch_event_target" "function_every_10_min" {
  arn       = aws_lambda_function.my_func.arn
  rule      = aws_cloudwatch_event_rule.every_10_min.name
}

resource "aws_lambda_permission" "allow_cloudwatch_to_call_function" {
  statement_id  = "AllowExecutionFromCloudWatch"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.my_func.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.every_10_min.arn
}