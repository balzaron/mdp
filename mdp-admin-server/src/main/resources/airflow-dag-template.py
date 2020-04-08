from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': days_ago(1),
    'retries': 1,
    'retry_delay': timedelta(minutes=1),
}

dag = DAG('${dag_id}', default_args=default_args, schedule_interval=${dag_schedule_interval!'timedelta(1)'})

<#list tasks as task>
task_${task.id} = BashOperator(task_id='${task.id}', bash_command='''${task.bash_command!''}''', retries = ${task.retries}, trigger_rule = '${task.trigger_rule}', dag=dag)
</#list>

<#list tasks as task>
    <#list task.parent_ids as parent_id>
task_${task.id}.set_upstream(task_${parent_id})
    </#list>
</#list>