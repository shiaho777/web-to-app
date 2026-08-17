/* Android NDK's compiler-rt drops the x86_80 soft routines, so the linker
 * misses __mulxc3 (complex long double multiply lowered by clang as a
 * libcall). Equivalent of compiler-rt lib/builtins/mulxc3.c. */
#include <math.h>

long double _Complex __mulxc3(long double a, long double b,
                              long double c, long double d)
{
	long double ac = a * c, bd = b * d, ad = a * d, bc = b * c;
	long double _Complex z;
	__real__ z = ac - bd;
	__imag__ z = ad + bc;
	if (isnan(__real__ z) && isnan(__imag__ z)) {
		int recalc = 0;
		if (isinf(a) || isinf(b)) {
			a = copysignl(isinf(a) ? 1 : 0, a);
			b = copysignl(isinf(b) ? 1 : 0, b);
			if (isnan(c)) c = copysignl(0, c);
			if (isnan(d)) d = copysignl(0, d);
			recalc = 1;
		}
		if (isinf(c) || isinf(d)) {
			c = copysignl(isinf(c) ? 1 : 0, c);
			d = copysignl(isinf(d) ? 1 : 0, d);
			if (isnan(a)) a = copysignl(0, a);
			if (isnan(b)) b = copysignl(0, b);
			recalc = 1;
		}
		if (!recalc && (isinf(ac) || isinf(bd) || isinf(ad) || isinf(bc))) {
			if (isnan(a)) a = copysignl(0, a);
			if (isnan(b)) b = copysignl(0, b);
			if (isnan(c)) c = copysignl(0, c);
			if (isnan(d)) d = copysignl(0, d);
			recalc = 1;
		}
		if (recalc) {
			__real__ z = INFINITY * (a * c - b * d);
			__imag__ z = INFINITY * (a * d + b * c);
		}
	}
	return z;
}
