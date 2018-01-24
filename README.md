# RuleView
这是一个可用于选择身高体重的横向竖向刻度尺

V1.0.0
当前提供可设置的属性为：

        RullView.setMin(100);   //设置刻度尺最小值
        RullView.setMax(250);   //设置刻度尺最大值
        RullView.setNumber(170);    //设置刻度尺第一次显示的位置
        RullView.setInterval(10);   //设置刻度尺的间距
        RullView.setTextOffset(20); //根据显示的数字自主调节刻度尺数字的左右位置

可根据回调获得当前选中数值

        verticalRullView.setRuleListener(new AllRulerCallback() {
            @Override
            public void onRulerSelected(int length, int value) {
                //获得即时显示的数字
                String number = String.valueOf(value);
            }
        });