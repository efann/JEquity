/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

// From https://bitbucket.org/sco0ter/extfx/src/729c935ec306d3d5a6c5ef28f9c43b4352f11deb/src/main/java/extfx/scene/chart/DateAxis.java?at=master&fileviewer=file-view-default
package com.beowurks.jequity.dao;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.chart.Axis;
import javafx.util.StringConverter;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * An axis that displays date and time values.
 * <p/>
 * Tick labels are usually automatically set and calculated depending on the range unless you explicitly {@linkplain #setTickLabelFormatter(javafx.util.StringConverter) set an formatter}.
 * <p/>
 * You also have the chance to specify fix lower and upper bounds, otherwise they are calculated by your data.
 * <p/>
 * <p/>
 * <h3>Screenshots</h3>
 * <p>
 * Displaying date values, ranging over several months:</p>
 * <img src="doc-files/DateAxisMonths.png" alt="DateAxisMonths" />
 * <p>
 * Displaying date values, ranging only over a few hours:</p>
 * <img src="doc-files/DateAxisHours.png" alt="DateAxisHours" />
 * <p/>
 * <p/>
 * <h3>Sample Usage</h3>
 * <pre>
 * {@code
 * ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();
 *
 * ObservableList<XYChart.Data<Date, Number>> series1Data = FXCollections.observableArrayList();
 * series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2012, 11, 15).getTime(), 2));
 * series1Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 5, 3).getTime(), 4));
 *
 * ObservableList<XYChart.Data<Date, Number>> series2Data = FXCollections.observableArrayList();
 * series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 0, 13).getTime(), 8));
 * series2Data.add(new XYChart.Data<Date, Number>(new GregorianCalendar(2014, 7, 27).getTime(), 4));
 *
 * series.add(new XYChart.Series<>("Series1", series1Data));
 * series.add(new XYChart.Series<>("Series2", series2Data));
 *
 * NumberAxis numberAxis = new NumberAxis();
 * DateAxis dateAxis = new DateAxis();
 * LineChart<Date, Number> lineChart = new LineChart<>(dateAxis, numberAxis, series);
 * }
 * </pre>
 *
 * @author Christian Schudt
 * @author Diego Cirujano
 */
public final class DateAxis extends Axis<Date>
{

  /**
   * These property are used for animation.
   */
  private final LongProperty currentLowerBound = new SimpleLongProperty(this, "currentLowerBound");

  private final LongProperty currentUpperBound = new SimpleLongProperty(this, "currentUpperBound");

  private final ObjectProperty<StringConverter<Date>> tickLabelFormatter = new ObjectPropertyBase<StringConverter<Date>>()
  {
    @Override
    protected void invalidated()
    {
      if (!DateAxis.this.isAutoRanging())
      {
        DateAxis.this.invalidateRange();
        DateAxis.this.requestAxisLayout();
      }
    }

    @Override
    public Object getBean()
    {
      return DateAxis.this;
    }

    @Override
    public String getName()
    {
      return "tickLabelFormatter";
    }
  };

  /**
   * Stores the min and max date of the list of dates which is used.
   * If {@link #autoRanging} is true, these values are used as lower and upper bounds.
   */
  private Date minDate, maxDate;

  private final ObjectProperty<Date> lowerBound = new ObjectPropertyBase<Date>()
  {
    @Override
    protected void invalidated()
    {
      if (!DateAxis.this.isAutoRanging())
      {
        DateAxis.this.invalidateRange();
        DateAxis.this.requestAxisLayout();
      }
    }

    @Override
    public Object getBean()
    {
      return DateAxis.this;
    }

    @Override
    public String getName()
    {
      return "lowerBound";
    }
  };

  private final ObjectProperty<Date> upperBound = new ObjectPropertyBase<Date>()
  {
    @Override
    protected void invalidated()
    {
      if (!DateAxis.this.isAutoRanging())
      {
        DateAxis.this.invalidateRange();
        DateAxis.this.requestAxisLayout();
      }
    }

    @Override
    public Object getBean()
    {
      return DateAxis.this;
    }

    @Override
    public String getName()
    {
      return "upperBound";
    }
  };

  // private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);

  private Object currentAnimationID;

  private Interval actualInterval = Interval.DECADE;

  /**
   * Default constructor. By default the lower and upper bound are calculated by the data.
   */
  public DateAxis()
  {
  }

  /**
   * Constructs a date axis with fix lower and upper bounds.
   *
   * @param lowerBound The lower bound.
   * @param upperBound The upper bound.
   */
  public DateAxis(final Date lowerBound, final Date upperBound)
  {
    this();
    this.setAutoRanging(false);
    this.setLowerBound(lowerBound);
    this.setUpperBound(upperBound);
  }

  /**
   * Constructs a date axis with a label and fix lower and upper bounds.
   *
   * @param axisLabel  The label for the axis.
   * @param lowerBound The lower bound.
   * @param upperBound The upper bound.
   */
  public DateAxis(final String axisLabel, final Date lowerBound, final Date upperBound)
  {
    this(lowerBound, upperBound);
    this.setLabel(axisLabel);
  }

  @Override
  public void invalidateRange(final List<Date> list)
  {
    super.invalidateRange(list);

    Collections.sort(list);
    if (list.isEmpty())
    {
      this.minDate = this.maxDate = new Date(Calendar.getInstance().getTimeInMillis());
    }
    else if (list.size() == 1)
    {
      this.minDate = this.maxDate = list.get(0);
    }
    else if (list.size() > 1)
    {
      this.minDate = list.get(0);
      this.maxDate = list.get(list.size() - 1);
    }
  }

  @Override
  protected Object autoRange(final double length)
  {
    if (this.isAutoRanging())
    {
      return new Object[]{this.minDate, this.maxDate};
    }
    else
    {
      if (this.getLowerBound() == null || this.getUpperBound() == null)
      {
        throw new IllegalArgumentException("If autoRanging is false, a lower and upper bound must be set.");
      }
      return this.getRange();
    }
  }

  @Override
  protected void setRange(final Object range, final boolean animating)
  {
    final Object[] r = (Object[]) range;
    final Date oldLowerBound = this.getLowerBound();
    final Date oldUpperBound = this.getUpperBound();
    final Date lower = (Date) r[0];
    final Date upper = (Date) r[1];
    this.setLowerBound(lower);
    this.setUpperBound(upper);

    if (animating)
    {
//            final Timeline timeline = new Timeline();
//            timeline.setAutoReverse(false);
//            timeline.setCycleCount(1);
//            final AnimationTimer timer = new AnimationTimer() {
//                @Override
//                public void handle(long l) {
//                    requestAxisLayout();
//                }
//            };
//            timer.start();
//
//            timeline.setOnFinished(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent actionEvent) {
//                    timer.stop();
//                    requestAxisLayout();
//                }
//            });
//
//            KeyValue keyValue = new KeyValue(currentLowerBound, lower.getTime());
//            KeyValue keyValue2 = new KeyValue(currentUpperBound, upper.getTime());
//
//            timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
//                    new KeyValue(currentLowerBound, oldLowerBound.getTime()),
//                    new KeyValue(currentUpperBound, oldUpperBound.getTime())),
//                    new KeyFrame(Duration.millis(3000), keyValue, keyValue2));
//            timeline.play();

   /*   this.animator.stop(this.currentAnimationID);
      this.currentAnimationID = this.animator.animate(
          new KeyFrame(Duration.ZERO,
              new KeyValue(this.currentLowerBound, oldLowerBound.getTime()),
              new KeyValue(this.currentUpperBound, oldUpperBound.getTime())
          ),
          new KeyFrame(Duration.millis(700),
              new KeyValue(this.currentLowerBound, lower.getTime()),
              new KeyValue(this.currentUpperBound, upper.getTime())
          )
      );
*/
    }
    else
    {
      this.currentLowerBound.set(this.getLowerBound().getTime());
      this.currentUpperBound.set(this.getUpperBound().getTime());
    }
  }

  @Override
  protected Object getRange()
  {
    return new Object[]{this.getLowerBound(), this.getUpperBound()};
  }

  @Override
  public double getZeroPosition()
  {
    return 0;
  }

  @Override
  public double getDisplayPosition(final Date date)
  {
    final double length = this.getSide().isHorizontal() ? this.getWidth() : this.getHeight();

    // Get the difference between the max and min date.
    final double diff = this.currentUpperBound.get() - this.currentLowerBound.get();

    // Get the actual range of the visible area.
    // The minimal date should start at the zero position, that's why we subtract it.
    final double range = length - this.getZeroPosition();

    // Then get the difference from the actual date to the min date and divide it by the total difference.
    // We get a value between 0 and 1, if the date is within the min and max date.
    final double d = (date.getTime() - this.currentLowerBound.get()) / diff;

    // Multiply this percent value with the range and add the zero offset.
    if (this.getSide().isVertical())
    {
      return this.getHeight() - d * range + this.getZeroPosition();
    }
    else
    {
      return d * range + this.getZeroPosition();
    }
  }

  @Override
  public Date getValueForDisplay(final double displayPosition)
  {
    final double length = this.getSide().isHorizontal() ? this.getWidth() : this.getHeight();

    // Get the difference between the max and min date.
    final double diff = this.currentUpperBound.get() - this.currentLowerBound.get();

    // Get the actual range of the visible area.
    // The minimal date should start at the zero position, that's why we subtract it.
    final double range = length - this.getZeroPosition();

    if (this.getSide().isVertical())
    {
      // displayPosition = getHeight() - ((date - lowerBound) / diff) * range + getZero
      // date = displayPosition - getZero - getHeight())/range * diff + lowerBound
      return new Date((long) ((displayPosition - this.getZeroPosition() - this.getHeight()) / -range * diff + this.currentLowerBound.get()));
    }
    else
    {
      // displayPosition = ((date - lowerBound) / diff) * range + getZero
      // date = displayPosition - getZero)/range * diff + lowerBound
      return new Date((long) ((displayPosition - this.getZeroPosition()) / range * diff + this.currentLowerBound.get()));
    }
  }

  @Override
  public boolean isValueOnAxis(final Date date)
  {
    return date.getTime() > this.currentLowerBound.get() && date.getTime() < this.currentUpperBound.get();
  }

  @Override
  public double toNumericValue(final Date date)
  {
    return date.getTime();
  }

  @Override
  public Date toRealValue(final double v)
  {
    return new Date((long) v);
  }

  @Override
  protected List<Date> calculateTickValues(final double v, final Object range)
  {
    final Object[] r = (Object[]) range;
    final Date lower = (Date) r[0];
    final Date upper = (Date) r[1];

    List<Date> dateList = new ArrayList<>();
    final Calendar calendar = Calendar.getInstance();

    // The preferred gap which should be between two tick marks.
    final double averageTickGap = 100;
    final double averageTicks = v / averageTickGap;

    final List<Date> previousDateList = new ArrayList<>();

    Interval previousInterval = Interval.values()[0];

    // Starting with the greatest interval, add one of each calendar unit.
    for (final Interval interval : Interval.values())
    {
      // Reset the calendar.
      calendar.setTime(lower);
      // Clear the list.
      dateList.clear();
      previousDateList.clear();
      this.actualInterval = interval;

      // Loop as long we exceeded the upper bound.
      while (calendar.getTime().getTime() <= upper.getTime())
      {
        dateList.add(new Date(calendar.getTimeInMillis()));
        calendar.add(interval.interval, interval.amount);
      }
      // Then check the size of the list. If it is greater than the amount of ticks, take that list.
      if (dateList.size() > averageTicks)
      {
        calendar.setTime(lower);
        // Recheck if the previous interval is better suited.
        while (calendar.getTime().getTime() <= upper.getTime())
        {
          previousDateList.add(new Date(calendar.getTimeInMillis()));
          calendar.add(previousInterval.interval, previousInterval.amount);
        }
        break;
      }

      previousInterval = interval;
    }
    if (previousDateList.size() - averageTicks > averageTicks - dateList.size())
    {
      dateList = previousDateList;
      this.actualInterval = previousInterval;
    }

    // At last add the upper bound.
    dateList.add(upper);

    final List<Date> evenDateList = this.makeDatesEven(dateList, calendar);
    // If there are at least three dates, check if the gap between the lower date and the second date is at least half the gap of the second and third date.
    // Do the same for the upper bound.
    // If gaps between dates are to small, remove one of them.
    // This can occur, e.g. if the lower bound is 25.12.2013 and years are shown. Then the next year shown would be 2014 (01.01.2014) which would be too narrow to 25.12.2013.
    if (evenDateList.size() > 2)
    {

      final Date secondDate = evenDateList.get(1);
      final Date thirdDate = evenDateList.get(2);
      final Date lastDate = evenDateList.get(dateList.size() - 2);
      final Date previousLastDate = evenDateList.get(dateList.size() - 3);

      // If the second date is too near by the lower bound, remove it.
      if (secondDate.getTime() - lower.getTime() < (thirdDate.getTime() - secondDate.getTime()) / 2)
      {
        evenDateList.remove(secondDate);
      }

      // If difference from the upper bound to the last date is less than the half of the difference of the previous two dates,
      // we better remove the last date, as it comes to close to the upper bound.
      if (upper.getTime() - lastDate.getTime() < (lastDate.getTime() - previousLastDate.getTime()) / 2)
      {
        evenDateList.remove(lastDate);
      }
    }

    return evenDateList;
  }

  @Override
  protected void layoutChildren()
  {
    if (!this.isAutoRanging())
    {
      this.currentLowerBound.set(this.getLowerBound().getTime());
      this.currentUpperBound.set(this.getUpperBound().getTime());
    }
    super.layoutChildren();
  }

  @Override
  protected String getTickMarkLabel(final Date date)
  {

    final StringConverter<Date> converter = this.getTickLabelFormatter();
    if (converter != null)
    {
      return converter.toString(date);
    }

    final DateFormat dateFormat;
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    if (this.actualInterval.interval == Calendar.YEAR && calendar.get(Calendar.MONTH) == 0 && calendar.get(Calendar.DATE) == 1)
    {
      dateFormat = new SimpleDateFormat("yyyy");
    }
    else if (this.actualInterval.interval == Calendar.MONTH && calendar.get(Calendar.DATE) == 1)
    {
      dateFormat = new SimpleDateFormat("MMM yy");
    }
    else
    {
      switch (this.actualInterval.interval)
      {
        case Calendar.DATE:
        case Calendar.WEEK_OF_YEAR:
        default:
          dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
          break;
        case Calendar.HOUR:
        case Calendar.MINUTE:
          dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
          break;
        case Calendar.SECOND:
          dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
          break;
        case Calendar.MILLISECOND:
          dateFormat = DateFormat.getTimeInstance(DateFormat.FULL);
          break;
      }
    }
    return dateFormat.format(date);
  }

  /**
   * Makes dates even, in the sense of that years always begin in January, months always begin on the 1st and days always at midnight.
   *
   * @param dates The list of dates.
   * @return The new list of dates.
   */
  private List<Date> makeDatesEven(final List<Date> dates, final Calendar calendar)
  {
    // If the dates contain more dates than just the lower and upper bounds, make the dates in between even.
    if (dates.size() > 2)
    {
      final List<Date> evenDates = new ArrayList<>();

      // For each interval, modify the date slightly by a few millis, to make sure they are different days.
      // This is because Axis stores each value and won't update the tick labels, if the value is already known.
      // This happens if you display days and then add a date many years in the future the tick label will still be displayed as day.
      for (int i = 0; i < dates.size(); i++)
      {
        calendar.setTime(dates.get(i));
        switch (this.actualInterval.interval)
        {
          case Calendar.YEAR:
            // If its not the first or last date (lower and upper bound), make the year begin with first month and let the months begin with first day.
            if (i != 0 && i != dates.size() - 1)
            {
              calendar.set(Calendar.MONTH, 0);
              calendar.set(Calendar.DATE, 1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 6);
            break;
          case Calendar.MONTH:
            // If its not the first or last date (lower and upper bound), make the months begin with first day.
            if (i != 0 && i != dates.size() - 1)
            {
              calendar.set(Calendar.DATE, 1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 5);
            break;
          case Calendar.WEEK_OF_YEAR:
            // Make weeks begin with first day of week?
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 4);
            break;
          case Calendar.DATE:
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 3);
            break;
          case Calendar.HOUR:
            if (i != 0 && i != dates.size() - 1)
            {
              calendar.set(Calendar.MINUTE, 0);
              calendar.set(Calendar.SECOND, 0);
            }
            calendar.set(Calendar.MILLISECOND, 2);
            break;
          case Calendar.MINUTE:
            if (i != 0 && i != dates.size() - 1)
            {
              calendar.set(Calendar.SECOND, 0);
            }
            calendar.set(Calendar.MILLISECOND, 1);
            break;
          case Calendar.SECOND:
            calendar.set(Calendar.MILLISECOND, 0);
            break;

        }
        evenDates.add(new Date(calendar.getTimeInMillis()));
      }

      return evenDates;
    }
    else
    {
      return dates;
    }
  }

  /**
   * Gets the lower bound of the axis.
   *
   * @return The property.
   * @see #getLowerBound()
   * @see #setLowerBound(java.util.Date)
   */
  public final ObjectProperty<Date> lowerBoundProperty()
  {
    return this.lowerBound;
  }

  /**
   * Gets the lower bound of the axis.
   *
   * @return The lower bound.
   * @see #lowerBoundProperty()
   */
  public final Date getLowerBound()
  {
    return this.lowerBound.get();
  }

  /**
   * Sets the lower bound of the axis.
   *
   * @param date The lower bound date.
   * @see #lowerBoundProperty()
   */
  public final void setLowerBound(final Date date)
  {
    this.lowerBound.set(date);
  }

  /**
   * Gets the upper bound of the axis.
   *
   * @return The property.
   * @see #getUpperBound() ()
   * @see #setUpperBound(java.util.Date)
   */
  public final ObjectProperty<Date> upperBoundProperty()
  {
    return this.upperBound;
  }

  /**
   * Gets the upper bound of the axis.
   *
   * @return The upper bound.
   * @see #upperBoundProperty()
   */
  public final Date getUpperBound()
  {
    return this.upperBound.get();
  }

  /**
   * Sets the upper bound of the axis.
   *
   * @param date The upper bound date.
   * @see #upperBoundProperty() ()
   */
  public final void setUpperBound(final Date date)
  {
    this.upperBound.set(date);
  }

  /**
   * Gets the tick label formatter for the ticks.
   *
   * @return The converter.
   */
  public final StringConverter<Date> getTickLabelFormatter()
  {
    return this.tickLabelFormatter.getValue();
  }

  /**
   * Sets the tick label formatter for the ticks.
   *
   * @param value The converter.
   */
  public final void setTickLabelFormatter(final StringConverter<Date> value)
  {
    this.tickLabelFormatter.setValue(value);
  }

  /**
   * Gets the tick label formatter for the ticks.
   *
   * @return The property.
   */
  public final ObjectProperty<StringConverter<Date>> tickLabelFormatterProperty()
  {
    return this.tickLabelFormatter;
  }

  /**
   * The intervals, which are used for the tick labels. Beginning with the largest interval, the axis tries to calculate the tick values for this interval.
   * If a smaller interval is better suited for, that one is taken.
   */
  private enum Interval
  {
    DECADE(Calendar.YEAR, 10),
    YEAR(Calendar.YEAR, 1),
    MONTH_6(Calendar.MONTH, 6),
    MONTH_3(Calendar.MONTH, 3),
    MONTH_1(Calendar.MONTH, 1),
    WEEK(Calendar.WEEK_OF_YEAR, 1),
    DAY(Calendar.DATE, 1),
    HOUR_12(Calendar.HOUR, 12),
    HOUR_6(Calendar.HOUR, 6),
    HOUR_3(Calendar.HOUR, 3),
    HOUR_1(Calendar.HOUR, 1),
    MINUTE_15(Calendar.MINUTE, 15),
    MINUTE_5(Calendar.MINUTE, 5),
    MINUTE_1(Calendar.MINUTE, 1),
    SECOND_15(Calendar.SECOND, 15),
    SECOND_5(Calendar.SECOND, 5),
    SECOND_1(Calendar.SECOND, 1),
    MILLISECOND(Calendar.MILLISECOND, 1);

    private final int amount;

    private final int interval;

    Interval(final int interval, final int amount)
    {
      this.interval = interval;
      this.amount = amount;
    }
  }
}