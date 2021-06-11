package com.pdfconverter.jpg2pdf.pdf.converter.utils.scheduler;

import io.reactivex.Scheduler;

public interface SchedulerProviderInterface {

    Scheduler computation();

    Scheduler io();

    Scheduler ui();
}
