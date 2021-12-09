import DataList.{EmptyList, NonEmptyList}

import scala.annotation.tailrec

object ListOps {

  /**
   * Функция fold "сворачивает" список из Т в один элемент типа Т.
   * Если в списке лишь один элемент, то он и вернётся, два - вернётся результат применения f к этим элементам,
   * больше двух - результат применения к f(f(f(...), a[i - 1]), a[i])
   * @param f функция свёртывания. Применяется попарно к предыдущему результату применения и i-ому элементу списка
   * @return None - если список пустой
   */
  def foldOption[T](f: (T, T) => T): DataList[T] => Option[T] = {
    case EmptyList => None

    case NonEmptyList(head, tail) => foldOption(f)(tail) match {
      case Some(value) => Some(f(head, value))
      case None => Some(head)
    }
  }


  /**
   * Используя foldOption[T](f: (T, T) => T) реализуйте суммирование всех элементов списка.
   * @return Если список пустой, то 0
   */
  def sum[T : Numeric](list: DataList[T]): T = {
    /**
     * Используйте для суммирования двух чисел любого типа (Int, Long, Double, Float etc)
     */
    def sumT(a: T, b: T) = implicitly[Numeric[T]].plus(a, b)

    foldOption(sumT)(list) match {
      case Some(value) => value
      case None => Numeric[T].zero
    }
  }

  /**
   * Фильтрация списка. Хвостовая рекурсия
   * @param filterFunc - фильтрующее правило (если f(a[i]) == true, то элемент остаётся в списке)
   */
  @tailrec
  private def filterImpl[T](filterFunc: T => Boolean)
    (buffer: DataList[T])
    (list: DataList[T]): DataList[T] = {

    def reverseDataList[A](buffer: DataList[A])
      (list: DataList[A]): DataList[A] = {
      list match {
        case EmptyList => buffer

        case NonEmptyList(head, tail) => {
          reverseDataList(NonEmptyList(head, buffer))(tail)
        }
      }
    }

    list match {
      case NonEmptyList(head, tail) =>

      if (filterFunc(head)) {
        filterImpl(filterFunc)(NonEmptyList(head, buffer))(tail)
      } else {
        filterImpl(filterFunc)(buffer)(tail)
      }

      case EmptyList => reverseDataList(list)(buffer)
    }
  }

  final def filter[T](f: T => Boolean): DataList[T] => DataList[T] = {
    filterImpl(f)(DataList.EmptyList)
  }

  final def map[A, B](f: A => B): DataList[A] => DataList[B] = {
    case DataList.EmptyList => DataList.EmptyList

    case DataList.NonEmptyList(head, tail) => {
      DataList.NonEmptyList(f(head), map(f)(tail))
    }
  }

  /**
   * Используя композицию функций реализуйте collect. Collect - комбинация filter и map.
   * В качестве фильтрующего правила нужно использовать f.isDefinedAt
   */
  def collect[A, B](f: PartialFunction[A, B]): DataList[A] => DataList[B] = {
    l => map(f)(filter(
      x => f isDefinedAt x
    )(l))
  }
}