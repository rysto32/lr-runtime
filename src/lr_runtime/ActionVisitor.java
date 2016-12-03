/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 *
 * @author rstone
 */
public interface ActionVisitor<R, T> {
    public R visitAccept(Accept a, T t);
    public R visitReduce(Reduce r, T t);
    public R visitReject(Reject r, T t);
    public R visitShift(Shift s, T t);
}
